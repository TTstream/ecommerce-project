package com.portfolio.commerceflow.member.api;

public record TokenReissueResponse(
        String tokenType,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {

    public static TokenReissueResponse bearer(
            String accessToken,
            long accessTokenExpiresIn,
            String refreshToken,
            long refreshTokenExpiresIn
    ) {
        return new TokenReissueResponse("Bearer", accessToken, accessTokenExpiresIn, refreshToken, refreshTokenExpiresIn);
    }
}
