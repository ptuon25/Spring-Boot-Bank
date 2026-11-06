package com.tuon.tbank.dto.person;

import com.tuon.tbank.enums.person.EmployeeStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Contains the status assigned to an employee.
 */
public record EmployeeStatusRequest(@NotNull EmployeeStatus status) {
}
