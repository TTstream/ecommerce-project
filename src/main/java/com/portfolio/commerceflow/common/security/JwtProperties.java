package com.portfolio.commerceflow.common.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenValidity
) {

    public JwtProperties {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters.");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("JWT issuer must not be blank.");
        }
        if (accessTokenValidity == null || accessTokenValidity.isNegative() || accessTokenValidity.isZero()) {
            throw new IllegalArgumentException("JWT access token validity must be positive.");
        }
    }
}
