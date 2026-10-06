package com.tuon.tbank.entity.person;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuon.tbank.entity.transaction.reversalTransaction.TransactionReversal;
import com.tuon.tbank.enums.EmployeeRole;
import com.tuon.tbank.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an employee of the bank, which is a type of person.
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
public class Employee extends Person {

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private LocalDate admissionDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private BigDecimal salary;

    @OneToMany(mappedBy = "reviewedBy", cascade = CascadeType.DETACH, orphanRemoval = true)
    private Set<TransactionReversal> transactionReversals = new HashSet<>();

    public Employee(){
        super();
    }

    public Employee(String name, LocalDate birthDate, String document, String email, String passwordHash,
                    EmployeeRole role, LocalDate admissionDate, EmployeeStatus status, BigDecimal salary) {
        super(null);
        this.setName(name);
        this.setBirthDate(birthDate);
        this.setDocument(document);
        this.setEmail(email);
        this.setPasswordHash(passwordHash);
        this.role = role;
        this.admissionDate = admissionDate;
        this.status = status;
        this.salary = salary;
    }
}
