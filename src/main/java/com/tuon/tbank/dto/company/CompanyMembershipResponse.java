package com.tuon.tbank.dto.company;

import com.tuon.tbank.enums.company.CompanyMemberRole;
import com.tuon.tbank.enums.company.MembershipStatus;

public record CompanyMembershipResponse(Long membershipId, Long clientId,
                                        String clientName, CompanyMemberRole role, MembershipStatus status) {
}
