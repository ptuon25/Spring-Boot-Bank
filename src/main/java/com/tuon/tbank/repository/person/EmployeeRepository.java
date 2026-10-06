package com.tuon.tbank.repository.person;

import com.tuon.tbank.entity.person.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access operations for employees.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
