package com.tuon.tbank.dto.account;

import com.tuon.tbank.entity.person.Client;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Represents the response for creating a checking account.
 *
 * @param id            the unique identifier of the checking account
 * @param accountNumber the account number of the checking account
 * @param openingDate   the date when the checking account was opened
 * @param owner         the owner of the checking account
 */
public record CheckingAccountCreateResponse(@Positive Long id,
                                            @NotBlank String accountNumber,
                                            @NotNull @PastOrPresent LocalDate openingDate,
                                            @NotBlank @NotNull Client owner) {
}
