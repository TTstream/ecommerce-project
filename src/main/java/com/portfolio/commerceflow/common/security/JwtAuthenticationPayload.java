package com.portfolio.commerceflow.common.security;

import com.portfolio.commerceflow.member.domain.Role;

public record JwtAuthenticationPayload(
        Long memberId,
        String email,
        Role role
) {
}
