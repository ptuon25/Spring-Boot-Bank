package com.tuon.tbank.enums.company;

/**
 * Represents the roles that a member can have within a company.
 *
 * <p>This enum is used to define the different levels of access and responsibilities
 * that a member can have within a company.</p>
 *
 * <p>The roles are as follows:</p>
 * <ul>
 *     <li>{@link #ADMIN}: An administrator of the company, with the ability to manage other members and settings.</li>
 *     <li>{@link #OPERATOR}: An operator of the company, with limited access to perform specific tasks.</li>
 *     <li>{@link #VIEWER}: A viewer of the company, with read-only access to information.</li>
 * </ul>
 */
public enum CompanyMemberRole {
    ADMIN,
    OPERATOR,
    VIEWER
}
