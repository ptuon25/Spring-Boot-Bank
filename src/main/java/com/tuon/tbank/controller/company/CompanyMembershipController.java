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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing client memberships in companies.
 *
 * <p>This controller provides endpoints for adding clients to companies, listing memberships, changing roles, and revoking memberships.</p>
 * <p>It ensures that only authorized users can perform these operations and that the data remains consistent.</p>
 */
@RestController
@RequestMapping("/companies/{companyId}/members")
public class CompanyMembershipController {

    private final CompanyMembershipService companyMembershipService;

    public CompanyMembershipController(
            CompanyMembershipService companyMembershipService) {
        this.companyMembershipService = companyMembershipService;
    }

    @PostMapping
    @PreAuthorize("@companyAuthorization.canManageMembers(#companyId, authentication)")
    public ResponseEntity<CompanyMembershipResponse> addMember(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyMembershipCreateRequest request) {

        CompanyMembership membership = companyMembershipService.addMember(
                companyId,
                request.clientId(),
                request.role()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(membership));
    }

    @GetMapping
    @PreAuthorize("@companyAuthorization.canManageMembers(#companyId, authentication)")
    public ResponseEntity<List<CompanyMembershipResponse>> listMembers(
            @PathVariable Long companyId) {

        List<CompanyMembershipResponse> response =
                companyMembershipService.listMembers(companyId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{membershipId}/role")
    @PreAuthorize("@companyAuthorization.canManageMembers(#companyId, authentication)")
    public ResponseEntity<CompanyMembershipResponse> changeRole(
            @PathVariable Long companyId,
            @PathVariable Long membershipId,
            @Valid @RequestBody CompanyMembershipRoleRequest request) {

        CompanyMembership membership = companyMembershipService.changeRole(
                companyId,
                membershipId,
                request.role()
        );

        return ResponseEntity.ok(toResponse(membership));
    }

    @PatchMapping("/{membershipId}/revoke")
    @PreAuthorize("@companyAuthorization.canManageMembers(#companyId, authentication)")
    public ResponseEntity<Void> revoke(
            @PathVariable Long companyId,
            @PathVariable Long membershipId) {

        companyMembershipService.revoke(companyId, membershipId);

        return ResponseEntity.noContent().build();
    }

    private CompanyMembershipResponse toResponse(
            CompanyMembership membership) {

        return new CompanyMembershipResponse(
                membership.getId(),
                membership.getClient().getId(),
                membership.getClient().getName(),
                membership.getRole(),
                membership.getStatus()
        );
    }
}
