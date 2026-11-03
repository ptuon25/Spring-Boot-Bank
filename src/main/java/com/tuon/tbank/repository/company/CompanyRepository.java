package com.tuon.tbank.repository.company;

import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.person.Person;
import com.tuon.tbank.enums.company.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Provides the data access operations for the {@link Company} entity, providing persistence operations
 * and database queries.
 *
 * <p>Extends {@link JpaRepository}, inheriting standard CRUD operations, such as
 * saving, retrieving by ID, and deleting records.</p>
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {
    /**
     * Retrieves all companies marked as logically deleted.
     *
     * @return A list of all companies marked as logically deleted.
     */
    List<Company> findByDeletedTrue();

    /**
     * Retrieves a company by ID, since it's not marked as deleted.
     *
     * @param id the unique company identifier
     * @return an {@link Optional} containing the company if found and not marked as deleted,
     * or an empty {@link Optional} otherwise
     */
    Optional<Company> findByIdAndDeletedFalse(Long id);

    /**
     * Retrieves a company by legal name, since it's not marked as deleted.
     *
     * @param legalName the company's legal name
     * @return an {@link Optional} containing the company if found and not marked as deleted,
     * or an empty {@link Optional} otherwise
     */
    Optional<Company> findByLegalNameAndDeletedFalse(String legalName);

    /**
     * Retrieves companies by trade name, since they are not marked as deleted.
     *
     * @param tradeName the company's trade name
     * @return a list of companies with the specified trade name
     */
    List<Company> findByTradeNameAndDeletedFalse(String tradeName);

    /**
     * Retrieves companies by partner, since they are not marked as deleted.
     *
     * @param partner the company's principal partner, who may be a client or employee
     * @return a list of companies with the specified partner
     */
    List<Company> findByPartnerAndDeletedFalse(Person partner);

    /**
     * Retrieves a company by registration number, since it's not marked as deleted.
     *
     * @param registrationNumber the company's registration number
     * @return an {@link Optional} containing the company if found and not marked as deleted,
     * or an empty {@link Optional} otherwise
     */
    Optional<Company> findByRegistrationNumberAndDeletedFalse(String registrationNumber);

    /**
     * Retrieves companies by their status, since they're not marked as deleted.
     *
     * @param status the company's status
     * @return a list of companies with the specified status
     */
    List<Company> findByStatusAndDeletedFalseOrderByLegalNameAsc(CompanyStatus status);

    /**
     * Retrieves companies by their foundation date range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of companies with foundation dates within the specified range
     */
    List<Company> findByFoundationDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves companies by their affiliation date range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of companies with affiliation dates within the specified range
     */
    List<Company> findByAffiliationDateBetweenAndDeletedFalse(LocalDate startDate, LocalDate endDate);

    /**
     * Checks if a company exists by registration number.
     *
     * @param registrationNumber the company's registration number
     * @return {@code true} if a company with the specified registration number exists, {@code false} otherwise
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Checks if another company uses the specified registration number.
     *
     * @param registrationNumber the registration number to check
     * @param id                 the identifier of the company to exclude
     * @return {@code true} if another company uses the registration number, {@code false} otherwise
     */
    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Long id);

    /**
     * Checks if a company exists by legal name.
     *
     * @param legalName the company's legal name
     * @return {@code true} if a company with the specified legal name exists, {@code false} otherwise
     */
    boolean existsByLegalName(String legalName);

    /**
     * Checks if another company uses the specified legal name.
     *
     * @param legalName the legal name to check
     * @param id        the identifier of the company to exclude
     * @return {@code true} if another company uses the legal name, {@code false} otherwise
     */
    boolean existsByLegalNameAndIdNot(String legalName, Long id);

    /**
     * Checks if a company exists by ID and partner ID, and is not marked as deleted.
     *
     * @param companyId the ID of the company to check
     * @param userId    the ID of the partner to check
     * @return {@code true} if a company with the specified ID and partner ID exists and is not marked as deleted, {@code false} otherwise
     */
    boolean existsByIdAndPartnerIdAndDeletedFalse(Long companyId, Long userId);
}
