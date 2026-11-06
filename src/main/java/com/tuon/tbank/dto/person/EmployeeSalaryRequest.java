package com.tuon.tbank.dto.person;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Contains the salary assigned to an employee.
 */
public record EmployeeSalaryRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal salary) {
}
