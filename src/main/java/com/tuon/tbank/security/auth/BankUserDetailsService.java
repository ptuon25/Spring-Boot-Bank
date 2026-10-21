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

@Service
public class BankUserDetailsService implements UserDetailsService {

    private static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    public BankUserDetailsService(ClientRepository clientRepository, EmployeeRepository employeeRepository) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Client> client = clientRepository.findByEmail(email);
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
