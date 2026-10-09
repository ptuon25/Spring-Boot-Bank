package com.tuon.tbank.entity.person;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuon.tbank.entity.account.CheckingAccount;
import com.tuon.tbank.entity.account.JointAccount;
import com.tuon.tbank.entity.account.SavingsAccount;
import com.tuon.tbank.enums.person.ClientStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a client of the bank, which is a type of person.
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client extends Person {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate affiliationDate;

    @Enumerated(EnumType.STRING)
    private ClientStatus status;

    @OneToMany(mappedBy = "owner")
    private Set<CheckingAccount> checkingAccounts = new HashSet<>();

    @ManyToMany(mappedBy = "owners")
    private Set<JointAccount> jointAccounts = new HashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<SavingsAccount> savingsAccounts = new HashSet<>();

    public Client(){
        super();
    }

    public Client(String name, LocalDate birthDate, String document, String email, String passwordHash, LocalDate affiliationDate, ClientStatus status) {
        super(null);
        this.setName(name);
        this.setBirthDate(birthDate);
        this.setDocument(document);
        this.setEmail(email);
        this.setPasswordHash(passwordHash);
        this.affiliationDate = affiliationDate;
        this.status = status;
    }
}
