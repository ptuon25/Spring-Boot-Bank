package com.tuon.tbank.dto.account;

import com.tuon.tbank.entity.person.Client;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Represents a request to create a checking account in the banking system.
 * This record encapsulates the necessary information for creating a checking account,
 * including the account details and the owner of the account.
 *
 * @param balance The initial balance of the checking account.
 * @param openingDate The date the checking account was opened.
 * @param owner The owner of the checking account.
 */
public record CheckingAccountCreateRequest(@NotNull BigDecimal balance,
                                           @NotNull @PastOrPresent LocalDate openingDate,
                                           @NotNull Client owner) {
}
