package com.tuon.tbank.enums.company;

/**
 * Lifecycle status of a client's membership in a company.
 *
 * <p>This enum is used to represent the current state of a client's membership in a company,
 * allowing the system to manage and track the membership status effectively.</p>
 *
 * <p>The possible values are:</p>
 * <ul>
 *     <li>{@link #ACTIVE}: The client is currently an active member of the company.</li>
 *     <li>{@link #INACTIVE}: The client is currently not an active member of the company.</li>
 *     <li>{@link #REVOKED}: The client's membership has been revoked.</li>
 * </ul>
 */
public enum MembershipStatus {
    ACTIVE,
    INACTIVE,
    REVOKED
}
