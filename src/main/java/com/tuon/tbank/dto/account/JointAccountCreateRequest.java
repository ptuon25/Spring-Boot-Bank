package com.tuon.tbank.dto.account;

import com.tuon.tbank.entity.person.Client;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a request to create a joint account in the banking system.
 * This record encapsulates the necessary information for creating a joint account,
 * including the account details and the owners of the account.
 *
 * @param balance The initial balance of the joint account.
 * @param openingDate The date the joint account was opened.
 * @param owner1 The first owner of the joint account.
 * @param owner2 The second owner of the joint account.
 */
public record JointAccountCreateRequest(@NotNull BigDecimal balance,
                                        @NotNull @PastOrPresent LocalDate openingDate,
                                        @NotNull Client owner1,
                                        @NotNull Client owner2) {
}
