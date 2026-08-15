package com.portfolio.commerceflow.member.application;

import com.portfolio.commerceflow.common.exception.BusinessException;
import com.portfolio.commerceflow.common.exception.ErrorCode;
import com.portfolio.commerceflow.common.security.JwtTokenProvider;
import com.portfolio.commerceflow.member.api.LoginRequest;
import com.portfolio.commerceflow.member.api.LoginResponse;
import com.portfolio.commerceflow.member.domain.Member;
import com.portfolio.commerceflow.member.infrastructure.MemberRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!member.isActive() || !passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole());
        return LoginResponse.bearer(accessToken, jwtTokenProvider.accessTokenExpiresInSeconds());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
