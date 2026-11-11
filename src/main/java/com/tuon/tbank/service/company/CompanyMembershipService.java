package com.tuon.tbank.service.company;

import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.entity.company.CompanyMembership;
import com.tuon.tbank.entity.person.Client;
import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;
import com.tuon.tbank.enums.person.ClientStatus;
import com.tuon.tbank.exception.serviceException.BusinessException;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.repository.company.CompanyMembershipRepository;
import com.tuon.tbank.repository.person.ClientRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Provides business logic for managing client memberships in companies.
 *
 * <p>This service handles the addition, listing, role changes, and revocation of client memberships within companies.</p>
 * <p>It ensures that only authorized users can perform these operations and that the data remains consistent.</p>
 * <p>The service also provides methods for checking the status of a client's membership and for updating the membership's role.</p>
 */
@Service
public class CompanyMembershipService {

    private final CompanyMembershipRepository companyMembershipRepository;
    private final CompanyService companyService;
    private final ClientRepository clientRepository;

    /**
     * Constructs a new CompanyMembershipService with the specified repositories and services.
     *
     * @param companyMembershipRepository the repository for managing company memberships
     * @param companyService              the service for managing companies
     * @param clientRepository            the repository for managing clients
     */
    public CompanyMembershipService(
            CompanyMembershipRepository companyMembershipRepository,
            CompanyService companyService,
            ClientRepository clientRepository) {
        this.companyMembershipRepository = companyMembershipRepository;
        this.companyService = companyService;
        this.clientRepository = clientRepository;
    }

    /**
     * Adds a client to a company or reactivates the client's existing membership.
     */
    @Transactional
    public CompanyMembership addMember(
            Long companyId,
            Long clientId,
            CompanyMemberRole role) {
        validateRole(role);
        if (companyId == null || companyId <= 0) {
            throw new BusinessException("Company ID must be greater than 0");
        }
        if (clientId == null || clientId <= 0) {
            throw new BusinessException("Client ID must be greater than 0");
        }

        Company company = getCompany(companyId);
        Client client = getActiveClient(clientId);

        if (company.getPartner() instanceof Client principalPartner
                && principalPartner.getId().equals(client.getId())) {
            throw new BusinessException(
                    "The company's principal partner cannot be added as a membership");
        }

        Optional<CompanyMembership> existing =
                companyMembershipRepository.findByCompanyIdAndClientId(
                        companyId,
                        clientId);

        if (existing.isPresent()) {
            CompanyMembership membership = existing.get();

            if (membership.getStatus() == MembershipStatus.ACTIVE) {
                throw new BusinessException(
                        "Client is already an active member of this company");
            }

            membership.setRole(role);
            membership.setStatus(MembershipStatus.ACTIVE);
            return companyMembershipRepository.save(membership);
        }

        CompanyMembership membership = new CompanyMembership(
                company,
                client,
                role,
                MembershipStatus.ACTIVE);

        return companyMembershipRepository.save(membership);
    }

    /**
     * Lists the active memberships of a company.
     */
    public List<CompanyMembership> listMembers(Long companyId) {
        getCompany(companyId);
        return companyMembershipRepository.findByCompanyIdAndStatus(
                companyId,
                MembershipStatus.ACTIVE);
    }

    /**
     * Changes the role of an active membership within the specified company.
     */
    @Transactional
    public CompanyMembership changeRole(
            Long companyId,
            Long membershipId,
            CompanyMemberRole role) {
        validateRole(role);
        getCompany(companyId);

        CompanyMembership membership = getMembership(membershipId, companyId);

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new BusinessException(
                    "Only an active membership can have its role changed");
        }

        if (membership.getRole() == role) {
            throw new BusinessException(
                    "The membership already has this role");
        }

        membership.setRole(role);
        return companyMembershipRepository.save(membership);
    }

    /**
     * Revokes an active membership within the specified company.
     */
    @Transactional
    public CompanyMembership revoke(
            Long companyId,
            Long membershipId) {
        getCompany(companyId);

        CompanyMembership membership = getMembership(membershipId, companyId);

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new BusinessException(
                    "Only an active membership can be revoked");
        }

        membership.setStatus(MembershipStatus.REVOKED);
        return companyMembershipRepository.save(membership);
    }

    /**
     * Retrieves a membership for a client in a company, regardless of status.
     */
    public Optional<CompanyMembership> getMembershipByCompanyIdAndClientId(
            Long companyId,
            Long clientId) {
        getCompany(companyId);
        return companyMembershipRepository.findByCompanyIdAndClientId(
                companyId,
                clientId);
    }

    /**
     * Retrieves memberships in a company with the requested status.
     */
    public List<CompanyMembership> getByCompanyIdAndStatus(
            Long companyId,
            MembershipStatus status) {
        if (status == null) {
            throw new BusinessException("Membership status cannot be null");
        }

        getCompany(companyId);
        return companyMembershipRepository.findByCompanyIdAndStatus(
                companyId,
                status);
    }

    /**
     * Retrieves a membership only when it belongs to the specified company.
     */
    public Optional<CompanyMembership> getMembershipById(
            Long membershipId,
            Long companyId) {
        getCompany(companyId);
        return companyMembershipRepository.findByIdAndCompanyId(
                membershipId,
                companyId);
    }

    /**
     * Retrieves a company by its ID, throwing an exception if not found.
     */
    private Company getCompany(Long companyId) {
        return companyService.getCompanyById(companyId);
    }

    /**
     * Retrieves an active client by its ID, throwing an exception if not found or inactive.
     */
    private Client getActiveClient(Long clientId) {
        Client client = clientRepository.findByIdAndDeletedFalse(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with ID: " + clientId));

        if (client.getStatus() != ClientStatus.ACTIVE) {
            throw new BusinessException("Client is not active");
        }

        return client;
    }

    /**
     * Retrieves a membership by its ID and company ID, throwing an exception if not found.
     */
    private CompanyMembership getMembership(
            Long membershipId,
            Long companyId) {
        return companyMembershipRepository.findByIdAndCompanyId(
                        membershipId,
                        companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Membership not found in this company"));
    }

    /**
     * Validates that the provided role is not null.
     */
    private void validateRole(CompanyMemberRole role) {
        if (role == null) {
            throw new BusinessException("Membership role cannot be null");
        }
    }
}
