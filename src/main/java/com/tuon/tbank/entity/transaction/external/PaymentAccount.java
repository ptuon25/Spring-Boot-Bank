package com.tuon.tbank.entity.transaction.external;

import com.tuon.tbank.entity.transaction.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a payment account in the system.
 */
@Entity
@Getter
@Setter
@Table(name = "payment_account")
public class PaymentAccount {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "destination")
    private Set<Payment> payments = new HashSet<>();

    public PaymentAccount() {
    }

    public PaymentAccount(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PaymentAccount that = (PaymentAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
