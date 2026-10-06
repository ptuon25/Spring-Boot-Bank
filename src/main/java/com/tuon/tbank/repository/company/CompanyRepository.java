package com.tuon.tbank.repository.company;

import com.tuon.tbank.entity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for companies.
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
