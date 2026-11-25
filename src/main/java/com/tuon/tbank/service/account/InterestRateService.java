package com.tuon.tbank.service.account;

import com.tuon.tbank.entity.account.InterestRateConfiguration;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.DatabaseException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.account.InterestRateConfigurationRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing interest rate configurations.
 * This class provides methods to retrieve interest rate configurations based on various criteria.
 */
@Service
public class InterestRateService {

    private final InterestRateConfigurationRepository interestRateConfigurationRepository;

    public InterestRateService(InterestRateConfigurationRepository interestRateConfigurationRepository) {
        this.interestRateConfigurationRepository = interestRateConfigurationRepository;
    }

    /**
     * Retrieves all interest rate configurations.
     *
     * @return a list of all InterestRateConfiguration entities
     */
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public List<InterestRateConfiguration> getAllInterestRateConfigurations() {
        return interestRateConfigurationRepository.findAll();
    }

    /**
     * Retrieves an interest rate configuration by its unique identifier.
     *
     * @param id the unique identifier of the interest rate configuration
     * @return the InterestRateConfiguration entity with the specified ID
     * @throws BusinessException if the ID is null or less than or equal to zero
     * @throws ResourceNotFoundException if no configuration is found for the specified ID
     */
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public InterestRateConfiguration getInterestRateConfigurationById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("Invalid interest rate configuration ID: " + id);
        }
        Optional<InterestRateConfiguration> interestRateConfiguration = interestRateConfigurationRepository.findById(id);
        return interestRateConfiguration.orElseThrow(() -> new ResourceNotFoundException("Interest rate configuration not found for id: " + id));
    }

    /**
     * Retrieves the interest rate configuration that is effective on a given date.
     *
     * @param effectiveDate the date for which to retrieve the interest rate configuration
     * @return the InterestRateConfiguration entity effective on the specified date
     * @throws BusinessException if the effective date is null or in the future
     * @throws ResourceNotFoundException if no configuration is found for the specified effective date
     */
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public InterestRateConfiguration getInterestRateConfigurationByEffectiveDate(LocalDate effectiveDate) {
        if (effectiveDate == null) {
            throw new BusinessException("Effective date cannot be null");
        }
        if (effectiveDate.isAfter(LocalDate.now())) {
            throw new BusinessException("Effective date cannot be in the future: " + effectiveDate);
        }
        Optional<InterestRateConfiguration> interestRateConfiguration = interestRateConfigurationRepository.findFirstByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(effectiveDate);
        return interestRateConfiguration.orElseThrow(() -> new ResourceNotFoundException("No interest rate configuration found for effective date: " + effectiveDate));
    }

    /**
     * Retrieves interest rate configurations created within a specified date range.
     *
     * @param createdAtDateAfter  the start date for the creation date range
     * @param createdAtDateBefore the end date for the creation date range
     * @return a list of InterestRateConfiguration entities created within the specified date range
     * @throws BusinessException if either date is null or if the start date is after the end date
     */
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public List<InterestRateConfiguration> getInterestRateConfigurationsByCreatedAtDateRange(LocalDateTime createdAtDateAfter, LocalDateTime createdAtDateBefore) {
        if (createdAtDateAfter == null || createdAtDateBefore == null) {
            throw new BusinessException("Creation date range cannot be null");
        }
        if (createdAtDateAfter.isAfter(createdAtDateBefore)) {
            throw new BusinessException("Creation start date cannot be after end date: " + createdAtDateAfter + " > " + createdAtDateBefore);
        }
        return interestRateConfigurationRepository.findByCreatedAtDateBetween(createdAtDateAfter, createdAtDateBefore);
    }

    /**
     * Registers a new interest rate configuration with the specified interest rate.
     * The effective date is calculated as the first day of the next month.
     *
     * @param interestRate the interest rate value for the new configuration
     * @return the newly created InterestRateConfiguration entity
     * @throws BusinessException if the interest rate is invalid or if a configuration already exists for the calculated effective date
     * @throws DatabaseException if there is a database constraint violation during the save operation
     */
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public InterestRateConfiguration registerInterestRateConfiguration(BigDecimal interestRate) {
        validateInterestRate(interestRate);
        LocalDate effectiveDate = calculateNextMonthEffectiveDate(LocalDate.now());
        ensureEffectiveDateIsAvailable(effectiveDate);
        LocalDateTime createdAt = LocalDateTime.now();
        InterestRateConfiguration configuration = buildConfiguration(interestRate, effectiveDate, createdAt);
        try {
            return interestRateConfigurationRepository.saveAndFlush(configuration);
        } catch (DataIntegrityViolationException e) {
            if (isEffectiveDateUniqueViolation(e)) {
                throw new BusinessException(
                        "An interest rate configuration already exists for the effective date: " + effectiveDate
                );
            }

            throw new DatabaseException(
                    "Could not create interest rate configuration due to a database constraint."
            );
        }
    }

    // AUXILIARY

    /**
     * Validates the provided interest rate value.
     *
     * @param interestRate the interest rate value to validate
     * @throws BusinessException if the interest rate is null or negative
     */
    private void validateInterestRate(BigDecimal interestRate) {
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) < 0.025) {
            throw new BusinessException("Interest rate must be a positive value greater than or equal to 0.025: " + interestRate);
        }
    }

    /**
     * Calculates the effective date for the next month.
     *
     * @param today the current date
     * @return the effective date for the next month
     */
    private LocalDate calculateNextMonthEffectiveDate(LocalDate today) {
        return today.withDayOfMonth(1).plusMonths(1);
    }

    /**
     * Ensures that there is no existing interest rate configuration for the specified effective date.
     *
     * @param effectiveDate the effective date to check for existing interest rate configurations
     * @throws BusinessException if an InterestRateConfiguration already exists for the specified effective date
     */
    private void ensureEffectiveDateIsAvailable(LocalDate effectiveDate) {
        if (interestRateConfigurationRepository.existsByEffectiveDate(effectiveDate)) {
            throw new BusinessException("An interest rate configuration already exists for the effective date: " + effectiveDate);
        }
    }

    /**
     * Builds a new InterestRateConfiguration object with the specified details.
     *
     * @param interestRate  the interest rate value for this configuration
     * @param effectiveDate the effective date from which this configuration is applicable
     * @param createdAt     the date and time when this configuration was created
     * @return a new InterestRateConfiguration object
     */
    private InterestRateConfiguration buildConfiguration(BigDecimal interestRate,
                                                         LocalDate effectiveDate,
                                                         LocalDateTime createdAt) {
        InterestRateConfiguration configuration = new InterestRateConfiguration();
        configuration.setInterestRate(interestRate);
        configuration.setEffectiveDate(effectiveDate);
        configuration.setCreatedAt(createdAt);
        return configuration;
    }

    /**
     * Checks if the provided Throwable is caused by a unique constraint violation on the effective date.
     *
     * @param error the Throwable to check
     * @return true if the Throwable is caused by a unique constraint violation on the effective date, false otherwise
     */
    private boolean isEffectiveDateUniqueViolation(Throwable error) {
        Throwable cause = error;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                return "uk_interest_rate_effective_date".equalsIgnoreCase(violation.getConstraintName());
            }
            cause = cause.getCause();
        }
        return false;
    }
}
