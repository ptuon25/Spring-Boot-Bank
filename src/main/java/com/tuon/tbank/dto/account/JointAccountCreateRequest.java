package com.tuon.tbank.dto.account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Represents a request to create a joint account in the banking system.
 * This record encapsulates the necessary information for creating a joint account,
 * including the account details and the owners of the account.
 *
 * @param owner1Id The ID of the first owner of the joint account. Must be a positive number and not null.
 * @param owner2Id The ID of the second owner of the joint account. Must be a positive number and not null.
 */
public record JointAccountCreateRequest(@NotNull @Positive Long owner1Id,
                                        @NotNull @Positive Long owner2Id) {
}
