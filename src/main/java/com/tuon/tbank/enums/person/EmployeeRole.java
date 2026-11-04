package com.tuon.tbank.enums.person;

/**
 * EmployeeRole enum represents the different roles an employee can have within the organization.
 *
 * <p>This enum is used to define the different levels of access and responsibility an employee may have.</p>
 *
 * <p>Roles:</p>
 * <ul>
 *     <li>{@link #ASSOCIATE} - A basic employee with limited access.</li>
 *     <li>{@link #MANAGER} - An employee with management responsibilities.</li>
 *     <li>{@link #ADMIN} - An employee with administrative privileges.</li>
 * </ul>
 */
public enum EmployeeRole {

    ASSOCIATE,
    MANAGER,
    ADMIN
}
