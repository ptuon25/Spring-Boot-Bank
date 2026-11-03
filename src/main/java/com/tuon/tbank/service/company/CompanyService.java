package com.tuon.tbank.service.company;

import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.person.Person;
import com.tuon.tbank.enums.company.CompanyStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.DatabaseException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.company.CompanyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Provides business logic for {@link Company} management, including registration,
 * retrieval, updates, status management, and logical deletion.
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    /**
     * Constructs a new instance of {@link CompanyService} with the specified dependencies.
     *
     * @param companyRepository the repository for accessing company data
     */
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;

    }

    /**
     * Retrieves all companies from the repository.
     *
     * @return a list of all companies
     */
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    /**
     * Retrieves a company by its unique identifier.
     *
     * @param id the unique identifier of the company
     * @return the {@link Company} with the specified ID
     * @throws BusinessException         if the ID is null or less than or equal to 0
     * @throws ResourceNotFoundException if no company is found with the specified ID
     */
    public Company getCompanyById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("Company ID must be greater than 0");
        }
        Optional<Company> company = companyRepository.findByIdAndDeletedFalse(id);
        return company.orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }

    /**
     * Retrieves all deleted companies from the repository.
     *
     * @return a list of all deleted companies
     */
    public List<Company> getAllDeletedCompanies() {
        return companyRepository.findByDeletedTrue();
    }

    /**
     * Retrieves companies by legal name or trade name.
     *
     * @param name the name of the company
     * @return a list of companies with the specified name
     * @throws BusinessException         if the name is null or blank
     * @throws ResourceNotFoundException if no companies are found with the specified name
     */
    public List<Company> getCompaniesByName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Company name cannot be null or blank");
        }
        String normalizedName = name.trim();
        LinkedHashMap<Long, Company> companiesById = new LinkedHashMap<>();
        companyRepository.findByLegalNameAndDeletedFalse(normalizedName)
                .ifPresent(company -> companiesById.put(company.getId(), company));
        companyRepository.findByTradeNameAndDeletedFalse(normalizedName)
                .forEach(company -> companiesById.putIfAbsent(company.getId(), company));
        if (companiesById.isEmpty()) {
            throw new ResourceNotFoundException("Company not found with name: " + normalizedName);
        }
        return List.copyOf(companiesById.values());
    }

    /**
     * Retrieves companies by their principal partner.
     *
     * @param partner the principal partner of the company, who may be a client or employee
     * @return a list of companies with the specified partner
     * @throws ResourceNotFoundException if no companies are found with the specified partner
     */
    public List<Company> getCompaniesByPartner(Person partner) {
        if (partner == null) {
            throw new BusinessException("Partner cannot be null");
        }
        List<Company> companies = companyRepository.findByPartnerAndDeletedFalse(partner);
        if (companies.isEmpty()) {
            throw new ResourceNotFoundException("Company not found with partner: " + partner.getId());
        }
        return companies;
    }

    /**
     * Retrieves a company by its registration number.
     *
     * @param registrationNumber the registration number of the company
     * @return an {@link Optional} containing the company if found and not marked as deleted,
     * or an empty {@link Optional} otherwise
     * @throws BusinessException if the registration number is null or blank
     */
    public Optional<Company> getCompanyByRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new BusinessException("Registration number cannot be null or blank");
        }
        return companyRepository.findByRegistrationNumberAndDeletedFalse(
                normalizeRegistrationNumber(registrationNumber));
    }

    /**
     * Retrieves companies by their foundation date range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of companies with foundation dates within the specified range
     * @throws BusinessException if the start date or end date is null, or if the start date is after the end date
     */
    public List<Company> getCompaniesByFoundationDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
        return companyRepository.findByFoundationDateBetweenAndDeletedFalse(startDate, endDate);
    }

    /**
     * Retrieves companies by their affiliation date range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of companies with affiliation dates within the specified range
     * @throws BusinessException if the start date or end date is null, or if the start date is after the end date
     */
    public List<Company> getCompaniesByAffiliationDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
        return companyRepository.findByAffiliationDateBetweenAndDeletedFalse(startDate, endDate);
    }

    /**
     * Retrieves companies by their status.
     *
     * @param status the status of the company
     * @return a list of companies with the specified status
     * @throws BusinessException if the status is null
     */
    public List<Company> getCompaniesByStatus(CompanyStatus status) {
        if (status == null) {
            throw new BusinessException("Company status cannot be null");
        }
        return companyRepository.findByStatusAndDeletedFalseOrderByLegalNameAsc(status);
    }

    /**
     * Registers a new company with the specified legal name, partner, registration number, and foundation date.
     *
     * @param legalName          the legal name of the company
     * @param tradeName          the trade name of the company
     * @param partner            the partner of the company
     * @param registrationNumber the registration number of the company
     * @param foundationDate     the date on which the company was founded
     * @return the newly registered {@link Company}
     * @throws BusinessException if any validation fails
     */
    @PreAuthorize("@companyAuthorization.canRegisterCompany(authentication)")
    public Company registerCompany(String legalName, String tradeName, Person partner, String registrationNumber,
                                   LocalDate foundationDate) {
        if (legalName == null || legalName.isBlank()) {
            throw new BusinessException("Company legal name cannot be null or blank");
        }
        if (tradeName == null || tradeName.isBlank()) {
            throw new BusinessException("Company trade name cannot be null or blank");
        }
        if (partner == null) {
            throw new BusinessException("Company partner cannot be null");
        }
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new BusinessException("Company registration number cannot be null or blank");
        }
        if (foundationDate == null) {
            throw new BusinessException("Company foundation date cannot be null");
        }
        if (foundationDate.isAfter(LocalDate.now())) {
            throw new BusinessException("Company foundation date cannot be in the future");
        }
        return createCompany(new Company(
                null,
                legalName,
                tradeName,
                partner,
                registrationNumber,
                foundationDate,
                LocalDate.now(),
                CompanyStatus.ACTIVE));
    }

    /**
     * Creates a new company in the repository after validating its data.
     *
     * @param company the company to be created
     * @return the newly created {@link Company}
     * @throws BusinessException if any validation fails
     */
    public Company createCompany(Company company) {
        validateCompany(company);
        String normalizedRegistrationNumber = normalizeRegistrationNumber(company.getRegistrationNumber());
        validateRegistrationNumber(normalizedRegistrationNumber);
        validateRegistrationNumberNotRegistered(normalizedRegistrationNumber, null);
        validateLegalName(company.getLegalName());
        validateLegalNameNotRegistered(company.getLegalName(), null);
        company.setRegistrationNumber(normalizedRegistrationNumber);
        try {
            return companyRepository.saveAndFlush(company);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not create company due to a database constraint.");
        }
    }

    /**
     * Updates the profile of an existing company.
     *
     * @param id      the ID of the company to update
     * @param company the updated company data
     * @return the updated {@link Company}
     * @throws BusinessException if any validation fails
     */
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public Company updateCompany(Long id, Company company) {
        if (company == null) {
            throw new BusinessException("Company cannot be null");
        }
        Company entity = getCompanyById(id);
        validateCompanyUpdate(company, id);
        String normalizedRegistrationNumber = normalizeRegistrationNumber(company.getRegistrationNumber());
        validateRegistrationNumber(normalizedRegistrationNumber);
        validateRegistrationNumberNotRegistered(normalizedRegistrationNumber, id);
        validateLegalNameNotRegistered(company.getLegalName(), id);
        updateData(entity, company);
        try {
            return companyRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not update company due to a database constraint.");
        }
    }

    /**
     * Blocks a company by setting its status to BLOCKED.
     *
     * @param id the ID of the company to block
     * @return the updated {@link Company} with status BLOCKED
     * @throws BusinessException if the company is already blocked or if any validation fails
     */
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public Company blockCompany(Long id) {
        Company company = getCompanyById(id);
        if (company.getStatus() == CompanyStatus.BLOCKED) {
            throw new BusinessException("Company is already blocked");
        }
        try {
            company.setStatus(CompanyStatus.BLOCKED);
            return companyRepository.saveAndFlush(company);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not update company due to a database constraint.");
        }
    }

    /**
     * Deactivates a company by setting its status to INACTIVE.
     *
     * @param id the ID of the company to deactivate
     * @return the updated {@link Company} with status INACTIVE
     * @throws BusinessException if the company is already inactive, blocked, or if any validation fails
     */
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public Company deactivateCompany(Long id) {
        Company company = getCompanyById(id);
        if (company.getStatus() == CompanyStatus.INACTIVE) {
            throw new BusinessException("Company is already inactive");
        }
        if (company.getStatus() == CompanyStatus.BLOCKED) {
            throw new BusinessException("Company is blocked and cannot be deactivated");
        }
        try {
            company.setStatus(CompanyStatus.INACTIVE);
            return companyRepository.saveAndFlush(company);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not update company due to a database constraint.");
        }
    }

    /**
     * Reactivates a company by setting its status to ACTIVE.
     *
     * @param id the ID of the company to reactivate
     * @return the updated {@link Company} with status ACTIVE
     * @throws BusinessException if the company is already active or if any validation fails
     */
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public Company reactivateCompany(Long id) {
        Company company = getCompanyById(id);
        if (company.getStatus() == CompanyStatus.ACTIVE) {
            throw new BusinessException("Company is already active");
        }
        try {
            company.setStatus(CompanyStatus.ACTIVE);
            return companyRepository.saveAndFlush(company);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not update company due to a database constraint.");
        }
    }

    /**
     * Deletes a company by marking it as deleted in the repository.
     *
     * @param id the ID of the company to delete
     * @return the deleted {@link Company}
     * @throws BusinessException if any validation fails
     */
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public void deleteCompany(Long id) {
        Company company = getCompanyById(id);
        try {
            company.setDeleted(true);
            companyRepository.saveAndFlush(company);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Could not delete company due to a database constraint.");
        }
    }

    /**
     * Updates the data of an existing company with the values from an updated company.
     *
     * @param existingCompany the existing company to be updated
     * @param updatedCompany  the company containing the updated values
     */
    private void updateData(Company existingCompany, Company updatedCompany) {
        existingCompany.setLegalName(updatedCompany.getLegalName());
        existingCompany.setTradeName(updatedCompany.getTradeName());
        existingCompany.setPartner(updatedCompany.getPartner());
        existingCompany.setRegistrationNumber(
                normalizeRegistrationNumber(updatedCompany.getRegistrationNumber()));
    }

    /**
     * Validates the provided company object for required fields and constraints.
     *
     * @param company the company to validate
     * @throws BusinessException if any validation fails
     */
    private void validateCompany(Company company) {
        if (company == null) {
            throw new BusinessException("Company cannot be null");
        }
        if (company.getLegalName() == null || company.getLegalName().isBlank()) {
            throw new BusinessException("Company legal name cannot be null or blank");
        }
        if (company.getTradeName() == null || company.getTradeName().isBlank()) {
            throw new BusinessException("Company trade name cannot be null or blank");
        }
        if (company.getPartner() == null) {
            throw new BusinessException("Company partner cannot be null");
        }
        if (company.getRegistrationNumber() == null || company.getRegistrationNumber().isBlank()) {
            throw new BusinessException("Company registration number cannot be null or blank");
        }
        if (company.getFoundationDate() == null) {
            throw new BusinessException("Company foundation date cannot be null");
        }
        if (company.getFoundationDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Company foundation date cannot be in the future");
        }
        if (company.getAffiliationDate() == null) {
            throw new BusinessException("Company affiliation date cannot be null");
        }
        if (company.getStatus() == null) {
            throw new BusinessException("Company status cannot be null");
        }
    }

    /**
     * Validates the data required to update an existing company.
     *
     * @param company the company data to validate
     * @param id      the ID of the company being updated
     * @throws BusinessException if any required value is missing
     */
    private void validateCompanyUpdate(Company company, Long id) {
        if (id == null) {
            throw new BusinessException("Company ID cannot be null");
        }
        if (company.getLegalName() == null || company.getLegalName().isBlank()) {
            throw new BusinessException("Company legal name cannot be null or blank");
        }
        if (company.getTradeName() == null || company.getTradeName().isBlank()) {
            throw new BusinessException("Company trade name cannot be null or blank");
        }
        if (company.getPartner() == null) {
            throw new BusinessException("Company partner cannot be null");
        }
        if (company.getRegistrationNumber() == null || company.getRegistrationNumber().isBlank()) {
            throw new BusinessException("Company registration number cannot be null or blank");
        }
    }

    /**
     * Validates that the provided registration number has fourteen characters:
     * twelve alphanumeric characters followed by two numeric check digits.
     *
     * @param registrationNumber the registration number to validate
     * @throws BusinessException if the registration number is blank or has an invalid format
     */
    private void validateRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new BusinessException("Company registration number cannot be null or blank");
        }
        String normalizedRegistrationNumber = normalizeRegistrationNumber(registrationNumber);
        if (!normalizedRegistrationNumber.matches("[A-Z0-9]{12}[0-9]{2}")) {
            throw new BusinessException("Company registration number must have 14 characters (12 alphanumeric and 2 numeric check digits)");
        }
    }

    /**
     * Validates that the registration number is not already registered.
     *
     * @param registrationNumber the registration number to validate
     * @param companyId          the ID of the company (if updating an existing company)
     * @throws BusinessException if the registration number is already registered
     */
    private void validateRegistrationNumberNotRegistered(String registrationNumber, Long companyId) {
        if (companyId == null) {
            if (companyRepository.existsByRegistrationNumber(registrationNumber)) {
                throw new BusinessException("Company registration number is already registered");
            }
        } else {
            if (companyRepository.existsByRegistrationNumberAndIdNot(registrationNumber, companyId)) {
                throw new BusinessException("Company registration number is already registered by another company");
            }
        }
    }

    /**
     * Validates that the provided legal name is not null or blank.
     *
     * @param legalName the legal name to validate
     * @throws BusinessException if the legal name is invalid
     */
    private void validateLegalName(String legalName) {
        if (legalName == null || legalName.isBlank()) {
            throw new BusinessException("Company legal name cannot be null or blank");
        }
    }

    /**
     * Validates that the legal name is not already registered.
     *
     * @param legalName the legal name to validate
     * @param companyId the ID of the company (if updating an existing company)
     * @throws BusinessException if the legal name is already registered
     */
    private void validateLegalNameNotRegistered(String legalName, Long companyId) {
        if (companyId == null) {
            if (companyRepository.existsByLegalName(legalName)) {
                throw new BusinessException("Company legal name is already registered");
            }
        } else {
            if (companyRepository.existsByLegalNameAndIdNot(legalName, companyId)) {
                throw new BusinessException("Company legal name is already registered by another company");
            }
        }
    }

    /**
     * Normalizes a registration number by removing dots, dashes, slashes, and whitespace,
     * and converting letters to uppercase.
     *
     * @param document the registration number to normalize
     * @return the normalized registration number
     */
    private String normalizeRegistrationNumber(String document) {
        return document.replaceAll("[.\\-/\\s]", "").toUpperCase(Locale.ROOT);
    }


}
