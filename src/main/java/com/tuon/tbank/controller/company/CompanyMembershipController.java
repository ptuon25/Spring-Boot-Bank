package com.tuon.tbank.controller.company;

import com.tuon.tbank.dto.company.CompanyMembershipCreateRequest;
import com.tuon.tbank.dto.company.CompanyMembershipResponse;
import com.tuon.tbank.dto.company.CompanyMembershipRoleRequest;
import com.tuon.tbank.entity.company.CompanyMembership;
import com.tuon.tbank.service.company.CompanyMembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes REST endpoints for managing client memberships in companies.
 */
@RestController
@RequestMapping("/companies/{companyId}/members")
public class CompanyMembershipController {

    private final CompanyMembershipService companyMembershipService;

    /**
     * Creates a membership controller with the required service.
     *
     * @param companyMembershipService the service responsible for membership business logic
     */
    public CompanyMembershipController(CompanyMembershipService companyMembershipService) {
        this.companyMembershipService = companyMembershipService;
    }

    /**
     * Adds a client to a company, or reactivates the client's previous membership.
     *
     * @param companyId the unique identifier of the company
     * @param request   the client identifier and the role to assign
     * @return the created membership, with HTTP 201 Created
     */
    @PostMapping
    @PreAuthorize("@companyAuthorization.canManageMembers(#companyId, authentication)")
    public ResponseEntity<CompanyMembershipResponse> addMember(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyMembershipCreateRequest request) {
        CompanyMembership membership = companyMembershipService.addMember(
                companyId, request.clientId(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(membership));
    }

    /**
     * Lists the active memberships of a company.
     *
     * @param companyId the unique identifier of the company
     * @return the list of active memberships
     */
    @GetMapping
    @PreAuthorize("@companyAuthorization.canManageMembers(#companyId, authentication)")
    public ResponseEntity<List<CompanyMembershipResponse>> listMembers(@PathVariable Long companyId) {
        List<CompanyMembershipResponse> response = companyMembershipService.listMembers(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Changes the role of an active membership.
     *
     * @param companyId    the unique identifier of the company
     * @param membershipId the unique identifier of the membership
     * @param request      the new role
     * @return the updated membership
     */
    @PatchMapping("/{membershipId}/role")
    @PreAuthorize("@companyAuthorization.canManageMembers(#companyId, authentication)")
    public ResponseEntity<CompanyMembershipResponse> changeRole(
            @PathVariable Long companyId,
            @PathVariable Long membershipId,
            @Valid @RequestBody CompanyMembershipRoleRequest request) {
        CompanyMembership membership = companyMembershipService.changeRole(
                companyId, membershipId, request.role());
        return ResponseEntity.ok(toResponse(membership));
    }

    /**
     * Revokes an active membership.
     *
     * @param companyId    the unique identifier of the company
     * @param membershipId the unique identifier of the membership
     * @return the revoked membership
     */
    @PatchMapping("/{membershipId}/revoke")
    @PreAuthorize("@companyAuthorization.canManageMembers(#companyId, authentication)")
    public ResponseEntity<CompanyMembershipResponse> revoke(
            @PathVariable Long companyId,
            @PathVariable Long membershipId) {
        CompanyMembership membership = companyMembershipService.revoke(companyId, membershipId);
        return ResponseEntity.ok(toResponse(membership));
    }

    private CompanyMembershipResponse toResponse(CompanyMembership membership) {
        return new CompanyMembershipResponse(
                membership.getId(),
                membership.getClient().getId(),
                membership.getClient().getName(),
                membership.getRole(),
                membership.getStatus());
    }
}