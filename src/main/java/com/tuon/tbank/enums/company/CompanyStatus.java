package com.tuon.tbank.enums.company;

/**
 * Represents the possible statuses of a company within the banking system.
 *
 * <p>This enumeration is used to indicate the current operational state of a company,
 * which can affect its ability to access banking services.</p>
 * <ul>
 *     <li>{@link #ACTIVE} - The company is active in the system.</li>
 *     <li>{@link #INACTIVE} - The company is inactive and cannot be
 *     treated as an active company.</li>
 *     <li>{@link #BLOCKED} - The company's access or operations are
 *     restricted according to the system's business rules.</li>
 * </ul>
 */
public enum CompanyStatus {

    ACTIVE,
    INACTIVE,
    BLOCKED
}
