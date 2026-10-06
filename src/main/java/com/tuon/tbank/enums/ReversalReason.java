package com.tuon.tbank.enums;

/**
 * Defines the reasons for requesting a transaction reversal.
 */
public enum ReversalReason {

    DUPLICATE_TRANSACTION,
    FRAUD,
    OPERATIONAL_ERROR,
    CUSTOMER_REQUEST,
    OTHER;
}
