package com.tuon.tbank.repository.account;

import com.tuon.tbank.entity.account.InterestRateConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing InterestRateConfiguration entities.
 * This interface extends JpaRepository, providing CRUD operations and custom query methods for interest rate configurations.
 */
public interface InterestRateConfigurationRepository extends JpaRepository<InterestRateConfiguration, Long> {

    /**
     * Finds the most recent InterestRateConfiguration that is effective on or before the specified date.
     *
     * @param effectiveDate the date to check for effective interest rate configurations
     * @return an Optional containing the most recent InterestRateConfiguration if found, or empty if not found
     */
    Optional<InterestRateConfiguration> findFirstByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(LocalDate effectiveDate);

    /**
     * Finds all InterestRateConfiguration entities that were created between the specified dates.
     *
     * @param createdAtDateAfter  the start date for the creation date range
     * @param createdAtDateBefore the end date for the creation date range
     * @return a list of InterestRateConfiguration objects created within the specified date range
     */
    @Query("SELECT i FROM InterestRateConfiguration i WHERE i.createdAt BETWEEN :createdAtDateAfter AND :createdAtDateBefore")
    List<InterestRateConfiguration> findByCreatedAtDateBetween(LocalDateTime createdAtDateAfter, LocalDateTime createdAtDateBefore);

    /**
     * Checks if an InterestRateConfiguration exists for the specified effective date.
     *
     * @param effectiveDate the effective date to check for existing interest rate configurations
     * @return true if an InterestRateConfiguration exists for the specified effective date, false otherwise
     */
    boolean existsByEffectiveDate(LocalDate effectiveDate);
}