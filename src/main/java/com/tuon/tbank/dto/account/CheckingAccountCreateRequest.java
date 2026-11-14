package com.tuon.tbank.dto.account;

import jakarta.validation.constraints.NotNull;


/**
 * Represents a request to create a checking account in the banking system.
 * This record encapsulates the necessary information for creating a checking account,
 * including the account details and the owner of the account.
 *
 * @param ownerId The ID of the owner of the checking account.
 */
public record CheckingAccountCreateRequest(@NotNull Long ownerId) {
}
