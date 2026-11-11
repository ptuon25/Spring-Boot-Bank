package com.tuon.tbank.controller.company;

import com.tuon.tbank.dto.auth.CompanyRegistrationRequest;
import com.tuon.tbank.dto.auth.CompanyRegistrationResponse;
import com.tuon.tbank.dto.company.CompanyFoundationDateUpdateRequest;
import com.tuon.tbank.dto.company.CompanyProfileResponse;
import com.tuon.tbank.dto.company.PartnerSummaryResponse;
import com.tuon.tbank.dto.company.CompanyUpdateRequest;
import com.tuon.tbank.entity.company.Company;
import com.tuon.tbank.enums.company.CompanyStatus;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.service.company.CompanyService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Exposes REST endpoints for company registration, retrieval, profile updates,
 * status management, and logical deletion.
 */
@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Constructs a CompanyController with the specified CompanyService.
     *
     * @param companyService the service for managing company operations
     */
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Retrieves the profiles of all companies.
     *
     * @return the list of company profiles, or 204 No Content if there are none
     */
    @GetMapping
    @PreAuthorize("@employeeAuthorization.canSearchCompanies(authentication)")
    public ResponseEntity<List<CompanyProfileResponse>> getAllCompanies() {
        List<CompanyProfileResponse> response = toProfiles(companyService.getAllCompanies());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves the profiles of all logically deleted companies.
     *
     * @return the list of deleted company profiles, or 204 No Content if there are none
     */
    @GetMapping("/deleted")
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public ResponseEntity<List<CompanyProfileResponse>> getAllDeletedCompanies() {
        List<CompanyProfileResponse> response = toProfiles(companyService.getAllDeletedCompanies());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves a company's profile by its unique identifier.
     *
     * @param id the unique identifier of the company
     * @return the company profile
     */
    @GetMapping("/{id}")
    @PreAuthorize("@companyAuthorization.canReadCompany(#id, authentication)")
    public ResponseEntity<CompanyProfileResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(mapToCompanyProfileResponse(companyService.getCompanyById(id)));
    }

    /**
     * Retrieves the profiles of the companies of a partner.
     *
     * @param partnerId the unique identifier of the partner
     * @return the list of matching company profiles
     */
    @GetMapping("/search/partner")
    @PreAuthorize("@employeeAuthorization.canSearchCompanies(authentication)")
    public ResponseEntity<List<CompanyProfileResponse>> getCompaniesByPartnerId(
            @RequestParam Long partnerId) {
        return ResponseEntity.ok(toProfiles(companyService.getCompaniesByPartnerId(partnerId)));
    }


    /**
     * Retrieves a company's profile by its registration number.
     *
     * @param registrationNumber the registration number to search for
     * @return the matching company profile
     */
    @GetMapping("/search/registration-number")
    @PreAuthorize("@employeeAuthorization.canSearchCompanies(authentication)")
    public ResponseEntity<CompanyProfileResponse> getCompanyByRegistrationNumber(
            @RequestParam String registrationNumber) {
        Company company = companyService.getCompanyByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with registration number: " + registrationNumber));
        return ResponseEntity.ok(mapToCompanyProfileResponse(company));
    }

    /**
     * Retrieves the profiles of companies founded within a date range.
     *
     * @param startDate the start of the range (yyyy-MM-dd)
     * @param endDate   the end of the range (yyyy-MM-dd)
     * @return the list of matching company profiles
     */
    @GetMapping("/search/foundation-date-range")
    @PreAuthorize("@employeeAuthorization.canSearchCompanies(authentication)")
    public ResponseEntity<List<CompanyProfileResponse>> getCompaniesByFoundationDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(toProfiles(
                companyService.getCompaniesByFoundationDateRange(startDate, endDate)));
    }

    /**
     * Retrieves the profiles of companies affiliated within a date range.
     *
     * @param startDate the start of the range (yyyy-MM-dd)
     * @param endDate   the end of the range (yyyy-MM-dd)
     * @return the list of matching company profiles
     */
    @GetMapping("/search/affiliation-date-range")
    @PreAuthorize("@employeeAuthorization.canSearchCompanies(authentication)")
    public ResponseEntity<List<CompanyProfileResponse>> getCompaniesByAffiliationDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(toProfiles(
                companyService.getCompaniesByAffiliationDateRange(startDate, endDate)));
    }

    /**
     * Retrieves the profiles of companies with the specified status.
     *
     * @param status the company status to search for
     * @return the list of matching company profiles
     */
    @GetMapping("/search/status")
    @PreAuthorize("@employeeAuthorization.canSearchCompanies(authentication)")
    public ResponseEntity<List<CompanyProfileResponse>> getCompaniesByStatus(
            @RequestParam CompanyStatus status) {
        return ResponseEntity.ok(toProfiles(companyService.getCompaniesByStatus(status)));
    }

    /**
     * Blocks a company and returns the updated profile.
     *
     * @param id the unique identifier of the company to block
     * @return the updated company profile
     */
    @PatchMapping("/{id}/block")
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public ResponseEntity<CompanyProfileResponse> blockCompany(@PathVariable Long id) {
        return ResponseEntity.ok(mapToCompanyProfileResponse(companyService.blockCompany(id)));
    }

    /**
     * Deactivates a company and returns the updated profile.
     *
     * @param id the unique identifier of the company to deactivate
     * @return the updated company profile
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public ResponseEntity<CompanyProfileResponse> deactivateCompany(@PathVariable Long id) {
        return ResponseEntity.ok(mapToCompanyProfileResponse(companyService.deactivateCompany(id)));
    }

    /**
     * Reactivates a company and returns the updated profile.
     *
     * @param id the unique identifier of the company to reactivate
     * @return the updated company profile
     */
    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public ResponseEntity<CompanyProfileResponse> reactivateCompany(@PathVariable Long id) {
        return ResponseEntity.ok(mapToCompanyProfileResponse(companyService.reactivateCompany(id)));
    }

    /**
     * Inserts a new company into the system.
     *
     * @param request the company registration request
     * @return the profile of the newly created company
     */
    @PostMapping
    public ResponseEntity<CompanyRegistrationResponse> insertCompany(
            @Valid @RequestBody CompanyRegistrationRequest request){
        Company company = companyService.registerCompany(
                request.legalName(),
                request.tradeName(),
                request.registrationNumber(),
                request.partnerId(),
                request.foundationDate()
        );
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(company.getId()).toUri();
        return ResponseEntity.created(uri).body(new CompanyRegistrationResponse(
                company.getId(),
                company.getTradeName(),
                company.getPartner().getId(),
                company.getRegistrationNumber()));
    }

    /**
     * Updates a company's profile.
     *
     * @param id the unique identifier of the company
     * @param request the updated company data
     * @return the updated company profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public ResponseEntity<CompanyProfileResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyUpdateRequest request) {
        return ResponseEntity.ok(mapToCompanyProfileResponse(
                companyService.updateCompany(
                        id,
                        request.legalName(),
                        request.tradeName(),
                        request.partnerId(),
                        request.registrationNumber())));
    }

    /**
     * Updates a company's foundation date.
     *
     * @param id the unique identifier of the company
     * @param request the new foundation date
     * @return the updated company profile
     */
    @PutMapping("/{id}/foundation-date")
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public ResponseEntity<CompanyProfileResponse> updateFoundationDate(
            @PathVariable Long id,
            @Valid @RequestBody CompanyFoundationDateUpdateRequest request) {
        return ResponseEntity.ok(mapToCompanyProfileResponse(
                companyService.updateFoundationDate(id, request.foundationDate())));
    }

    /**
     * Logically deletes a company from the system.
     *
     * @param id the unique identifier of the company to delete
     * @return a response indicating successful deletion
     */
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("@employeeAuthorization.canManageCompany(authentication)")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Maps a list of Company objects to a list of CompanyProfileResponse objects.
     *
     * @param companies the list of Company objects to map
     * @return the list of mapped CompanyProfileResponse objects
     */
    private List<CompanyProfileResponse> toProfiles(List<Company> companies) {
        return companies.stream().map(this::mapToCompanyProfileResponse).toList();
    }

    /**
     * Maps a Company object to a CompanyProfileResponse object.
     *
     * @param company the Company object to map
     * @return the mapped CompanyProfileResponse object
     */
    private CompanyProfileResponse mapToCompanyProfileResponse(Company company) {
        return new CompanyProfileResponse(
                company.getId(),
                company.getTradeName(),
                company.getRegistrationNumber(),
                new PartnerSummaryResponse(
                        company.getPartner().getId(),
                        company.getPartner().getName()));
    }
    }