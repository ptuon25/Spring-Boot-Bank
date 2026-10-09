package com.tuon.tbank.entity.company;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.entity.audit.AuditRecord;
import com.tuon.tbank.entity.person.Person;
import com.tuon.tbank.enums.company.CompanyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a company, client of the bank.
 */
@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String legalName;
    private String tradeName;

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Person partner;

    private String registrationNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate foundationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate affiliationDate;

    @Enumerated(EnumType.STRING)
    private CompanyStatus status;

    @OneToMany(mappedBy = "owner")
    private Set<BusinessAccount> businessAccounts = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "company")
    private Set<AuditRecord> auditRecords = new HashSet<>();

    public Company(){

    }

    public Company(Long id, String legalName, String tradeName, Person partner, String registrationNumber, LocalDate foundationDate, LocalDate affiliationDate, CompanyStatus status) {
        this.id = id;
        this.legalName = legalName;
        this.tradeName = tradeName;
        this.partner = partner;
        this.registrationNumber = registrationNumber;
        this.foundationDate = foundationDate;
        this.affiliationDate = affiliationDate;
        this.status = status;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Company company = (Company) o;
        return getId() != null && Objects.equals(getId(), company.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
