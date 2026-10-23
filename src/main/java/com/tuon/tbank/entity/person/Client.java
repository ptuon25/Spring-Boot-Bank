package com.tuon.tbank.entity.person;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuon.tbank.entity.account.CheckingAccount;
import com.tuon.tbank.entity.account.JointAccount;
import com.tuon.tbank.entity.account.SavingsAccount;
import com.tuon.tbank.enums.person.ClientStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a client of the bank, which is a type of person.
 *
 * <p>This entity extends {@link Person} to inherit the client's
 * personal information and stores client-specific data, including
 * the affiliation date, current status, and associated bank accounts.</p>
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client extends Person {

    /**
     * Stores the date on which the client became affiliated with the bank.
     *
     * <p>Its JSON representation uses the {@code yyyy-MM-dd} date format.</p>
     *
     * <p>This value is assigned by the system when the client registers and
     * is not changed by any service operation.</p>
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate affiliationDate;

    /**
     * Represents the client's status in the bank system.
     *
     * <p>The available values are defined by {@link ClientStatus}.</p>
     *
     * <p>Persisted as text (the enum constant name, e.g. {@code "ACTIVE"}),
     * as configured by {@code @Enumerated(EnumType.STRING)}.</p>
     */
    @Enumerated(EnumType.STRING)
    private ClientStatus status;


    /**
     * Stores the checking accounts associated with this client.
     *
     * <p>This is the inverse side of the relationship, mapped by the
     * {@code owner} field in {@link CheckingAccount}.</p>
     *
     * <p>No setter is generated for this field. Changes to the relationship
     * must be made through the owning side, since the inverse side is not
     * persisted.</p>
     */
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "owner")
    private Set<CheckingAccount> checkingAccounts = new HashSet<>();


    /**
     * Stores the joint accounts associated with this client.
     *
     * <p>This is the inverse side of the many-to-many relationship,
     * mapped by the {@code owners} field in {@link JointAccount}.</p>
     *
     * <p>No setter is generated for this field. Changes to the relationship
     * must be made through the owning side, since the inverse side is not
     * persisted.</p>
     */
    @Setter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "owners")
    private Set<JointAccount> jointAccounts = new HashSet<>();


    /**
     * Stores the savings accounts associated with this client.
     *
     * <p>This is the inverse side of the relationship, mapped by the
     * {@code owner} field in {@link SavingsAccount}.</p>
     *
     * <p>No setter is generated for this field. Changes to the relationship
     * must be made through the owning side, since the inverse side is not
     * persisted.</p>
     */
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "owner")
    private Set<SavingsAccount> savingsAccounts = new HashSet<>();

    /**
     * Creates an empty client instance.
     *
     * <p>This constructor is required by JPA for entity instantiation.</p>
     */
    public Client() {
        super();
    }

    /**
     * Creates a client with the specified personal information,
     * affiliation date, and status.
     *
     * @param name            the client's full name
     * @param birthDate       the client's date of birth
     * @param document        the client's identification document
     * @param email           the client's email address
     * @param passwordHash    the client's encoded password
     * @param affiliationDate the date on which the client became affiliated with the bank
     * @param status          the client's initial status
     */
    public Client(String name, LocalDate birthDate, String document, String email, String passwordHash, LocalDate affiliationDate, ClientStatus status) {
        super();
        this.setName(name);
        this.setBirthDate(birthDate);
        this.setDocument(document);
        this.setEmail(email);
        this.setPasswordHash(passwordHash);
        this.affiliationDate = affiliationDate;
        this.status = status;
    }
}
