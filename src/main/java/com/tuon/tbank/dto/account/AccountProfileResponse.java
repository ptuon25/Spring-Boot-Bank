package com.tuon.tbank.dto.account;

import com.tuon.tbank.entity.account.Account;
import com.tuon.tbank.enums.account.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Represents the basic account data returned by the API.
 *
 * <p>Allows the API to expose only necessary information about the account, avoiding
 * exposing unnecessary or sensitive information from the {@link Account} entity.</p>
 *
 * @param id            the account's identifier in the database
 * @param accountNumber the account's number
 * @param openingDate   the account's opening date
 * @param status        the account's status
 */
public record AccountProfileResponse(Long id,
                                     String accountNumber,
                                     LocalDate openingDate,
                                     AccountStatus status) {
}
