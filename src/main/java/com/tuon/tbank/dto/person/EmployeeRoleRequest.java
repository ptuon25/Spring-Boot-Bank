package com.tuon.tbank.dto.person;

import com.tuon.tbank.enums.person.EmployeeRole;
import jakarta.validation.constraints.NotNull;

/**
 * Contains the role assigned to an employee.
 */
public record EmployeeRoleRequest(@NotNull EmployeeRole role) {
}
