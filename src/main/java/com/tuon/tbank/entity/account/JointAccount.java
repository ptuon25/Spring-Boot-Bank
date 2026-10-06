package com.tuon.tbank.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a joint account, which is a type of bank account owned by two clients.
 */
@Entity
@Table(name = "joint_accounts")
@Getter
@Setter
public class JointAccount extends Account {

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "joint_account_owner", joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "client_id"))
    private List<Client> owners;

    public JointAccount(){
        super();
    }

    public JointAccount(List<Client> owners) {
        this.owners = owners;
    }

    public JointAccount(Long id, String accountNumber, BigDecimal balance, LocalDate openingDate, AccountStatus status, List<Client> owners) {
        super(id, accountNumber, balance, openingDate, status);
        this.owners = owners;
    }
}
