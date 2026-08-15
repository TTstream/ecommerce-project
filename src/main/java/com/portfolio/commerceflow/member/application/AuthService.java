package com.portfolio.commerceflow.member.application;

import com.portfolio.commerceflow.common.exception.BusinessException;
import com.portfolio.commerceflow.common.exception.ErrorCode;
import com.portfolio.commerceflow.common.security.JwtTokenProvider;
import com.portfolio.commerceflow.common.security.JwtAuthenticationPayload;
import com.portfolio.commerceflow.member.api.LoginRequest;
import com.portfolio.commerceflow.member.api.LoginResponse;
import com.portfolio.commerceflow.member.api.TokenReissueRequest;
import com.portfolio.commerceflow.member.api.TokenReissueResponse;
import com.portfolio.commerceflow.member.domain.Member;
import com.portfolio.commerceflow.member.infrastructure.MemberRepository;
import java.time.Duration;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public AuthService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenStore refreshTokenStore
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!member.isActive() || !passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getEmail(), member.getRole());
        refreshTokenStore.save(member.getId(), refreshToken, refreshTokenTtl());

        return LoginResponse.bearer(
                accessToken,
                jwtTokenProvider.accessTokenExpiresInSeconds(),
                refreshToken,
                jwtTokenProvider.refreshTokenExpiresInSeconds()
        );
    }

    public TokenReissueResponse reissue(TokenReissueRequest request) {
        if (!jwtTokenProvider.isRefreshToken(request.refreshToken())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        JwtAuthenticationPayload payload = jwtTokenProvider.parse(request.refreshToken());
        String savedRefreshToken = refreshTokenStore.findByMemberId(payload.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!savedRefreshToken.equals(request.refreshToken())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findById(payload.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getEmail(), member.getRole());
        refreshTokenStore.save(member.getId(), refreshToken, refreshTokenTtl());

        return TokenReissueResponse.bearer(
                accessToken,
                jwtTokenProvider.accessTokenExpiresInSeconds(),
                refreshToken,
                jwtTokenProvider.refreshTokenExpiresInSeconds()
        );
    }

    public void logout(Long memberId) {
        refreshTokenStore.deleteByMemberId(memberId);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Duration refreshTokenTtl() {
        return Duration.ofSeconds(jwtTokenProvider.refreshTokenExpiresInSeconds());
    }
}
