package com.tuon.tbank.dto.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO representing the response for interest rate configuration.
 * This record encapsulates the details of an interest rate configuration, including its ID, interest rate,
 * effective date, and creation timestamp.
 *
 * @param id            the unique identifier of the interest rate configuration
 * @param interestRate  the interest rate value
 * @param effectiveDate the date when the interest rate becomes effective
 * @param createdAt     the timestamp when the configuration was created
 */
public record InterestRateConfigurationResponse(Long id,
                                                BigDecimal interestRate,
                                                LocalDate effectiveDate,
                                                LocalDateTime createdAt) {
}
