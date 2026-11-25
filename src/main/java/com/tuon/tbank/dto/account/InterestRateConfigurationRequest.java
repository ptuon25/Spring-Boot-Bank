package com.tuon.tbank.dto.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for interest rate configuration requests.
 * This class represents the request payload for creating or updating interest rate configurations.
 *
 * @param interestRate the interest rate value, which must be a non-null BigDecimal with a minimum value of 0.0025
 */
public record InterestRateConfigurationRequest(@NotNull @DecimalMin("0.0025") @Digits(integer = 3, fraction = 8) BigDecimal interestRate) {
}
