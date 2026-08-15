package com.portfolio.commerceflow.member.api;

import com.portfolio.commerceflow.member.domain.Member;
import com.portfolio.commerceflow.member.domain.Role;

public record MemberSignupResponse(
        Long id,
        String email,
        String name,
        Role role
) {

    public static MemberSignupResponse from(Member member) {
        return new MemberSignupResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole()
        );
    }
}
