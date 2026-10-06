package com.tuon.tbank.config;

import com.tuon.tbank.entity.account.BusinessAccount;
import com.tuon.tbank.entity.account.CheckingAccount;
import com.tuon.tbank.entity.account.JointAccount;
import com.tuon.tbank.entity.account.SavingsAccount;
import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.entity.person.Employee;
import com.tuon.tbank.entity.transaction.external.ExternalAccount;
import com.tuon.tbank.entity.transaction.external.ExternalBank;
import com.tuon.tbank.entity.transaction.external.PaymentAccount;
import com.tuon.tbank.enums.*;
import com.tuon.tbank.repository.account.BusinessAccountRepository;
import com.tuon.tbank.repository.account.CheckingAccountRepository;
import com.tuon.tbank.repository.account.JointAccountRepository;
import com.tuon.tbank.repository.account.SavingsAccountRepository;
import com.tuon.tbank.repository.company.CompanyRepository;
import com.tuon.tbank.repository.person.ClientRepository;
import com.tuon.tbank.repository.person.EmployeeRepository;
import com.tuon.tbank.repository.transaction.ExternalAccountRepository;
import com.tuon.tbank.repository.transaction.ExternalBankRepository;
import com.tuon.tbank.repository.transaction.PaymentAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Configures sample banking data for the test profile.
 */
@Configuration
@Profile("test")
public class TestConfig implements CommandLineRunner {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private BusinessAccountRepository businessAccountRepository;

    @Autowired
    private CheckingAccountRepository checkingAccountRepository;

    @Autowired
    private JointAccountRepository jointAccountRepository;

    @Autowired
    private SavingsAccountRepository savingsAccountRepository;
    @Autowired
    private ExternalBankRepository externalBankRepository;
    @Autowired
    private ExternalAccountRepository externalAccountRepository;
    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    @Override
    public void run(String... args) throws Exception {

        Client client1 = new Client("John Doe", LocalDate.of(1990, 10, 15),
                "1234567890", "john_doe@hotmail.com", "hashed_password_1", LocalDate.of(2020, 11, 3)
                , ClientStatus.ACTIVE);
        Client client2 = new Client("Jane Smith", LocalDate.of(1984, 7, 12),
                "0987654321", "jane_smith@gmail.com", "hashed_password_2", LocalDate.of(2021, 4, 15),
                ClientStatus.ACTIVE);

        clientRepository.saveAll(Arrays.asList(client1, client2));

        Employee employee1 = new Employee("Alice Johnson", LocalDate.of(1978, 12, 10),
                "1112223330", "alice_johnson@tbank.com", "password01", EmployeeRole.MANAGER,
                LocalDate.of(2015, 3, 20), EmployeeStatus.ACTIVE, BigDecimal.valueOf(8500.00));
        Employee employee2 = new Employee("Bob Williams", LocalDate.of(1985, 5, 22),
                "4445556660", "bob_williams@tbank.com", "password02", EmployeeRole.ASSOCIATE,
                LocalDate.of(2018, 8, 15), EmployeeStatus.ACTIVE, BigDecimal.valueOf(6500.00));

        employeeRepository.saveAll(Arrays.asList(employee1, employee2));

        Company company1 = new Company(null, "Tech Solutions Ltda", "Tech Solutions", client1, "123.456.789-0",
                LocalDate.of(1995,2,20), LocalDate.of(2019,3,14),
                CompanyStatus.ACTIVE);
        Company company2 = new Company(null, "Green Energy Inc", "Green Energy", client2, "987.654.321-0",
                LocalDate.of(2005,6,10), LocalDate.of(2020,3,14),
                CompanyStatus.ACTIVE);

        companyRepository.saveAll(Arrays.asList(company1, company2));

        BusinessAccount bAccount1 = new BusinessAccount(null, "000001", BigDecimal.valueOf(10000.00),
                LocalDate.of(2023,1,15), AccountStatus.ACTIVE, company1);
        BusinessAccount bAccount2 = new BusinessAccount(null, "000002", BigDecimal.valueOf(20000.00),
                LocalDate.of(2024,12,1), AccountStatus.ACTIVE, company2);

        CheckingAccount cAccount = new CheckingAccount(null, "000003", BigDecimal.valueOf(5000.00),
                LocalDate.of(2022,5,10), AccountStatus.ACTIVE, client1);

        JointAccount jAccount = new JointAccount(null, "000004", BigDecimal.valueOf(2500.00),
                LocalDate.of(2024,2,1), AccountStatus.ACTIVE, Arrays.asList(client1, client2));

        SavingsAccount sAccount = new SavingsAccount(null, "000005", BigDecimal.valueOf(1500.00),
                LocalDate.of(2025,2,1), AccountStatus.ACTIVE, client2, BigDecimal.valueOf(0.03));

        businessAccountRepository.saveAll(Arrays.asList(bAccount1, bAccount2));
        checkingAccountRepository.save(cAccount);
        jointAccountRepository.save(jAccount);
        savingsAccountRepository.save(sAccount);

        ExternalBank americanBank = new ExternalBank(null, "American Bank", "United States of America", "USD", "AMERUS33");
        ExternalBank europeanBank = new ExternalBank(null, "European Bank", "Germany", "EUR", "EURDE33");

        externalBankRepository.saveAll(Arrays.asList(americanBank, europeanBank));

        ExternalAccount americanAccount = new ExternalAccount(null, "AMER000001", "Frank Richards", BigDecimal.valueOf(1000.00), americanBank);
        ExternalAccount europeanAccount = new ExternalAccount(null, "EUR000001", "Sophie Müller", BigDecimal.valueOf(2000.00), europeanBank);

        externalAccountRepository.saveAll(Arrays.asList(americanAccount, europeanAccount));

        PaymentAccount paymentAccount = new PaymentAccount(null, "Payment Account");

        paymentAccountRepository.save(paymentAccount);



    }

}
