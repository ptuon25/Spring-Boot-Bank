package com.tuon.tbank.entity.person;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.audit.AuditRecord;
import com.tuon.tbank.entity.transaction.reversalTransaction.TransactionReversal;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a person in the system, which can be either a client or a employee.
 */
@Entity
@Getter
@Setter
@Table(name = "person")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Person {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String document;

    @Column(nullable = false, unique = true)
    private String email;

    //@Setter(AccessLevel.NONE) -> create method to set new password
    @JsonIgnore
    private String passwordHash;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "requestedBy", cascade = CascadeType.DETACH, orphanRemoval = true)
    private Set<TransactionReversal> transactionReversals = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @OneToMany(mappedBy = "performedBy")
    private Set<AuditRecord> auditRecordsPerformedBy = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @OneToMany(mappedBy = "person")
    private Set<AuditRecord> auditRecordsPerson = new HashSet<>();

    private boolean deleted = false;

    public Person(){}

    public Person(Long id, String name, LocalDate birthDate, String document, String email, String passwordHash) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.document = document;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? o.getClass().getSuperclass()
                : o.getClass();

        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? this.getClass().getSuperclass()
                : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;

        Person person = (Person) o;

        return getId() != null && Objects.equals(getId(), person.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? this.getClass().getSuperclass().hashCode()
                : getClass().hashCode();
    }
}
