package com.portfolio.commerceflow.common.security;

import com.portfolio.commerceflow.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String EMAIL_CLAIM = "email";
    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecretKey secretKey;

    @Autowired
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this(jwtProperties, Clock.systemUTC());
    }

    JwtTokenProvider(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long memberId, String email, Role role) {
        return createToken(memberId, email, role, JwtTokenType.ACCESS, jwtProperties.accessTokenValidity());
    }

    public String createRefreshToken(Long memberId, String email, Role role) {
        return createToken(memberId, email, role, JwtTokenType.REFRESH, jwtProperties.refreshTokenValidity());
    }

    public JwtAuthenticationPayload parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new JwtAuthenticationPayload(
                Long.valueOf(claims.getSubject()),
                claims.get(EMAIL_CLAIM, String.class),
                Role.valueOf(claims.get(ROLE_CLAIM, String.class)),
                JwtTokenType.valueOf(claims.get(TOKEN_TYPE_CLAIM, String.class))
        );
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long accessTokenExpiresInSeconds() {
        return jwtProperties.accessTokenValidity().toSeconds();
    }

    public long refreshTokenExpiresInSeconds() {
        return jwtProperties.refreshTokenValidity().toSeconds();
    }

    public boolean isAccessToken(String token) {
        try {
            return parse(token).tokenType() == JwtTokenType.ACCESS;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return parse(token).tokenType() == JwtTokenType.REFRESH;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private String createToken(
            Long memberId,
            String email,
            Role role,
            JwtTokenType tokenType,
            java.time.Duration validity
    ) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(validity);

        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(memberId))
                .id(UUID.randomUUID().toString())
                .claim(EMAIL_CLAIM, email)
                .claim(ROLE_CLAIM, role.name())
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
}
