package com.portfolio.commerceflow.member.application;

import com.portfolio.commerceflow.common.exception.BusinessException;
import com.portfolio.commerceflow.common.exception.ErrorCode;
import com.portfolio.commerceflow.member.api.MemberSignupRequest;
import com.portfolio.commerceflow.member.api.MemberSignupResponse;
import com.portfolio.commerceflow.member.domain.Member;
import com.portfolio.commerceflow.member.infrastructure.MemberRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberSignupService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberSignupService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MemberSignupResponse signup(MemberSignupRequest request) {
        String email = normalizeEmail(request.email());

        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = Member.createUser(email, encodedPassword, request.name().trim());
        Member savedMember = memberRepository.save(member);

        return MemberSignupResponse.from(savedMember);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
