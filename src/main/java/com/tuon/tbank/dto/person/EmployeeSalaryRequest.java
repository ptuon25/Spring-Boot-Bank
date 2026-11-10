package com.tuon.tbank.dto.person;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Contains the salary assigned to an employee.
 *
 * <p>This record is used to update the salary of an employee.</p>
 *
 * @param salary the salary assigned to the employee, must be a positive value greater than or equal to 0.01
 */
public record EmployeeSalaryRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal salary) {
}
