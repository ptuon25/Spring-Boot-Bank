package com.tuon.tbank.dto.person;

import com.tuon.tbank.enums.person.EmployeeRole;
import jakarta.validation.constraints.NotNull;

/**
 * Contains the role assigned to an employee.
 *
 * <p>This record is used to update the role of an employee.</p>
 *
 * @param role the role assigned to the employee, must not be null
 */
public record EmployeeRoleRequest(@NotNull EmployeeRole role) {
}
