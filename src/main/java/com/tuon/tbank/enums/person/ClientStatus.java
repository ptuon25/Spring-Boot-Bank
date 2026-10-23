package com.tuon.tbank.enums.person;

/**
 * Defines the possible statuses of a client in the
 * {@link com.tuon.tbank.entity.person.Client} entity.
 *
 * <p>These statuses indicate whether a client is active, inactive,
 * or blocked within the banking system.</p>
 *
 * <ul>
 *     <li>{@link #ACTIVE} - The client is active in the system.</li>
 *     <li>{@link #INACTIVE} - The client is inactive and cannot be
 *     treated as an active client.</li>
 *     <li>{@link #BLOCKED} - The client's access or operations are
 *     restricted according to the system's business rules.</li>
 * </ul>
 */
public enum ClientStatus {

    ACTIVE,
    INACTIVE,
    BLOCKED

}
