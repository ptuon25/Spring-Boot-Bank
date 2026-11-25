package com.tuon.tbank.controller.account;

import com.tuon.tbank.dto.account.InterestRateConfigurationRequest;
import com.tuon.tbank.dto.account.InterestRateConfigurationResponse;
import com.tuon.tbank.entity.account.InterestRateConfiguration;
import com.tuon.tbank.exception.serviceException.ResourceNotFoundException;
import com.tuon.tbank.service.account.InterestRateService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/interest-rates")
public class InterestRateController {

    private final InterestRateService interestRateService;

    public InterestRateController(InterestRateService interestRateService) {
        this.interestRateService = interestRateService;
    }

    /**
     * Retrieves all interest rate configurations.
     *
     * @return a ResponseEntity containing a list of InterestRateConfigurationResponse objects
     */
    @GetMapping
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public ResponseEntity<List<InterestRateConfigurationResponse>> getAllInterestRateConfigurations() {
        List<InterestRateConfigurationResponse> response = toProfiles(interestRateService.getAllInterestRateConfigurations());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Retrieves an interest rate configuration by its ID.
     *
     * @param id the ID of the interest rate configuration
     * @return a ResponseEntity containing the InterestRateConfigurationResponse object if found
     * @throws ResourceNotFoundException if no configuration is found for the given ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public ResponseEntity<InterestRateConfigurationResponse> getInterestRateConfigurationById(@PathVariable Long id) {
        InterestRateConfiguration configuration = interestRateService.getInterestRateConfigurationById(id);
        return ResponseEntity.ok(mapToConfigurationResponse(configuration));
    }

    /**
     * Retrieves an interest rate configuration by its effective date.
     *
     * @param effectiveDate the effective date of the interest rate configuration
     * @return a ResponseEntity containing the InterestRateConfigurationResponse object if found
     * @throws ResourceNotFoundException if no configuration is found for the given effective date
     */
    @GetMapping("/effective-date/current")
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public ResponseEntity<InterestRateConfigurationResponse> getInterestRateConfigurationByEffectiveDate(@RequestParam LocalDate effectiveDate) {
        InterestRateConfiguration configuration = interestRateService.getInterestRateConfigurationByEffectiveDate(effectiveDate);
        return ResponseEntity.ok(mapToConfigurationResponse(configuration));
    }

    /**
     * Retrieves interest rate configurations created within a specified date range.
     *
     * @param createdAtDateAfter  the start date of the range (inclusive)
     * @param createdAtDateBefore the end date of the range (inclusive)
     * @return a ResponseEntity containing a list of InterestRateConfigurationResponse objects
     */
    @GetMapping("/search/created-at-range")
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public ResponseEntity<List<InterestRateConfigurationResponse>> getInterestRateConfigurationsByCreatedAtRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtDateAfter,
                                                                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtDateBefore) {
        List<InterestRateConfigurationResponse> response = toProfiles(interestRateService.getInterestRateConfigurationsByCreatedAtDateRange(createdAtDateAfter, createdAtDateBefore));
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    /**
     * Creates a new interest rate configuration.
     *
     * @param request the InterestRateConfigurationRequest object containing the interest rate details
     * @return a ResponseEntity containing the created InterestRateConfigurationResponse object
     */
    @PostMapping
    @PreAuthorize("@accountAuthorization.canAdministerAccount(authentication)")
    public ResponseEntity<InterestRateConfigurationResponse> createInterestRateConfiguration(@Valid @RequestBody InterestRateConfigurationRequest request) {
        InterestRateConfiguration configuration = interestRateService.registerInterestRateConfiguration(request.interestRate());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(configuration.getId()).toUri();
        return ResponseEntity.created(uri).body(new InterestRateConfigurationResponse(
                configuration.getId(),
                configuration.getInterestRate(),
                configuration.getEffectiveDate(),
                configuration.getCreatedAt()
        ));
    }

    //AUXILIARY

    /**
     * Maps an InterestRateConfiguration entity to an InterestRateConfigurationResponse DTO.
     *
     * @param configuration the InterestRateConfiguration entity to be mapped
     * @return an InterestRateConfigurationResponse DTO representing the mapped configuration
     */
    private InterestRateConfigurationResponse mapToConfigurationResponse(InterestRateConfiguration configuration) {
        return new InterestRateConfigurationResponse(
                configuration.getId(),
                configuration.getInterestRate(),
                configuration.getEffectiveDate(),
                configuration.getCreatedAt()
        );
    }

    /**
     * Maps a list of InterestRateConfiguration entities to a list of InterestRateConfigurationResponse DTOs.
     *
     * @param configurations the list of InterestRateConfiguration entities to be mapped
     * @return a list of InterestRateConfigurationResponse DTOs representing the mapped configurations
     */
    private List<InterestRateConfigurationResponse> toProfiles(List<InterestRateConfiguration> configurations) {
        return configurations.stream()
                .map(this::mapToConfigurationResponse)
                .toList();
    }
}
