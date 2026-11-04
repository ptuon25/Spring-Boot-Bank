package com.tuon.tbank.dto.company;

import com.tuon.tbank.enums.company.CompanyMemberRole;
import jakarta.validation.constraints.NotNull;

public record CompanyMembershipRoleRequest(@NotNull CompanyMemberRole role) {
}
