package com.portfolio.commerceflow.member.api;

import com.portfolio.commerceflow.member.domain.Member;
import com.portfolio.commerceflow.member.domain.MemberStatus;
import com.portfolio.commerceflow.member.domain.Role;

public record MemberProfileResponse(
        Long id,
        String email,
        String name,
        Role role,
        MemberStatus status
) {

    public static MemberProfileResponse from(Member member) {
        return new MemberProfileResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getStatus()
        );
    }
}
