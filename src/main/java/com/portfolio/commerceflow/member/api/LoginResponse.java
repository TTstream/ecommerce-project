package com.portfolio.commerceflow.member.api;

public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresIn
) {

    public static LoginResponse bearer(String accessToken, long expiresIn) {
        return new LoginResponse("Bearer", accessToken, expiresIn);
    }
}
