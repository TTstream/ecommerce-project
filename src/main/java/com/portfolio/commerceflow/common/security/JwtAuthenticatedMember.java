package com.portfolio.commerceflow.common.security;

import com.portfolio.commerceflow.member.domain.Role;

public record JwtAuthenticatedMember(
        Long memberId,
        String email,
        Role role
) {
}
