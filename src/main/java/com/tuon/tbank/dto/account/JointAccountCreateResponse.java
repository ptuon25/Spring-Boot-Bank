package com.tuon.tbank.dto.account;

import com.tuon.tbank.entity.person.Client;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Represents the response for creating a joint account.
 *
 * <p>This record is used to convey the result of a joint account creation request,
 * including the unique identifier, account number, opening date, and the two owners
 * of the joint account.</p>
 *
 * @param id The unique identifier of the joint account.
 * @param accountNumber The account number of the joint account.
 * @param openingDate The date when the joint account was opened.
 * @param owner1 The first owner of the joint account.
 * @param owner2 The second owner of the joint account.
 */
public record JointAccountCreateResponse(@Positive Long id,
                                         @NotBlank String accountNumber,
                                         @NotNull @PastOrPresent LocalDate openingDate,
                                         @NotBlank @NotNull Client owner1,
                                         @NotBlank @NotNull Client owner2) {
}
