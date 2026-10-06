package com.tuon.tbank.entity.transaction.external;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an external bank entity.
 */
@Entity
@Getter
@Setter
@Table(name = "external_banks")
public class ExternalBank {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String country;
    private String currency;
    private String swiftCode;

    @OneToMany(mappedBy = "externalBank")
    private Set<ExternalAccount> externalAccounts = new HashSet<>();

    public ExternalBank() {
    }

    public ExternalBank(Long id, String name, String country, String currency, String swiftCode) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.currency = currency;
        this.swiftCode = swiftCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExternalBank that = (ExternalBank) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
