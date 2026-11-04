package com.tuon.tbank.dto.company;

import com.tuon.tbank.enums.company.CompanyMemberRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompanyMembershipCreateRequest(
        @NotNull @Positive Long clientId,
        @NotNull CompanyMemberRole role) {
}
