package com.tuon.tbank.security.auth;

import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.entity.person.Employee;
import com.tuon.tbank.enums.auth.UserIdentityType;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.enums.person.EmployeeStatus;
import com.tuon.tbank.repository.person.ClientRepository;
import com.tuon.tbank.repository.person.EmployeeRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Loads client and employee accounts for Spring Security authentication.
 */
@Service
public class BankUserDetailsService implements UserDetailsService {

    private static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Creates the user-details service with the client and employee repositories.
     *
     * @param clientRepository the repository used to look up non-deleted clients
     * @param employeeRepository the repository used to look up employees
     */
    public BankUserDetailsService(ClientRepository clientRepository, EmployeeRepository employeeRepository) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Loads a client or employee by email and maps the account data to Spring Security user details.
     *
     * <p>The email must identify exactly one account. Client accounts must have a non-blank password hash;
     * employee accounts must also have a non-null role. Only accounts with ACTIVE status are enabled.</p>
     *
     * @param email the email address used to look up the account
     * @return the corresponding Spring Security user details
     * @throws UsernameNotFoundException if no unique account is found or its required authentication
     *                                    data is missing
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Client> client = clientRepository.findByEmailAndDeletedFalse(email);
        Optional<Employee> employee = employeeRepository.findByEmail(email);

        if (client.isPresent() == employee.isPresent()) {
            throw new UsernameNotFoundException(INVALID_CREDENTIALS);
        }

        if (client.isPresent()) {
            Client account = client.get();
            if (account.getPasswordHash() == null || account.getPasswordHash().isBlank()) {
                throw new UsernameNotFoundException(INVALID_CREDENTIALS);
            }
            return new BankUserDetails(account.getId(), account.getEmail(), account.getPasswordHash(),
                    UserIdentityType.CLIENT, "ROLE_CLIENT", account.getStatus() == ClientStatus.ACTIVE);
        }

        Employee account = employee.orElseThrow(() -> new UsernameNotFoundException(INVALID_CREDENTIALS));
        if (account.getPasswordHash() == null || account.getPasswordHash().isBlank() || account.getRole() == null) {
            throw new UsernameNotFoundException(INVALID_CREDENTIALS);
        }
        return new BankUserDetails(account.getId(), account.getEmail(), account.getPasswordHash(),
                UserIdentityType.EMPLOYEE, "ROLE_" + account.getRole().name(),
                account.getStatus() == EmployeeStatus.ACTIVE);
    }
}
