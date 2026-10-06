package com.tuon.tbank.entity.transaction.external;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.transaction.Transfer;
import com.tuon.tbank.entity.transaction.endpoint.TransferEndpoint;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an account held at an external bank.
 */
@Entity
@Getter
@Setter
@Table(name = "external_accounts")
public class ExternalAccount implements TransferEndpoint {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;
    private String accountHolderName;
    private BigDecimal balance;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "external_bank_id", nullable = false)
    private ExternalBank externalBank;

    @OneToMany(mappedBy = "sourceExternalAccount")
    private Set<Transfer> sourceTransfers;

    @OneToMany(mappedBy = "destinationExternalAccount")
    private Set<Transfer> destinationTransfers;

    public ExternalAccount(){}

    public ExternalAccount(Long id, String accountNumber, String accountHolderName, BigDecimal balance, ExternalBank externalBank) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
        this.externalBank = externalBank;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExternalAccount that = (ExternalAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
