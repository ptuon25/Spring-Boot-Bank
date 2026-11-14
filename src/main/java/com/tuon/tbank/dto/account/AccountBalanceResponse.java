package com.tuon.tbank.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Represents the response containing account balance information.
 *
 * <p>This record is used to encapsulate the account ID, account number, and balance
 * for a specific bank account.</p>
 *
 * @param accountId     The unique identifier of the account.
 * @param accountNumber The account number associated with the account.
 * @param balance       The current balance of the account.
 */
public record AccountBalanceResponse( Long accountId,
                                      String accountNumber,
                                      BigDecimal balance) {
}
