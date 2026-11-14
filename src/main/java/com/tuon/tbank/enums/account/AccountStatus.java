package com.tuon.tbank.enums.account;

/**
 * Defines the statuses that can be assigned to a bank account.
 *
 * <p>The statuses are: ACTIVE, BLOCKED, and SUSPENDED.</p>
 * <ul>
 *     <li>{@link #ACTIVE}: The account is active and can be used for transactions.</li>
 *     <li>{@link #BLOCKED}: The account is blocked and cannot be used for transactions.</li>
 *     <li>{@link #SUSPENDED}: The account is suspended and cannot be used for transactions.</li>
 * </ul>
 */
public enum AccountStatus {
    ACTIVE,
    BLOCKED,
    SUSPENDED
}
