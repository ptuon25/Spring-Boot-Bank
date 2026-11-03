package com.tuon.tbank.enums.person;

/**
 * Represents the possible statuses of an employee within the banking system.
 *
 * <p>This enumeration is used to indicate the current operational state of an employee,
 * which can affect their ability to access banking services.</p>
 * <ul>
 *     <li>{@link #ACTIVE} - The employee is active in the system.</li>
 *     <li>{@link #ON_LEAVE} - The employee is on leave and cannot be
 *     treated as an active employee.</li>
 *     <li>{@link #TERMINATED} - The employee's access or operations are
 *     terminated according to the system's business rules.</li>
 * </ul>
 */
public enum EmployeeStatus {

    ACTIVE,
    ON_LEAVE,
    TERMINATED
}
