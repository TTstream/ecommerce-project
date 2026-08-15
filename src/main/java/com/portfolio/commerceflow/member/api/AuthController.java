package com.portfolio.commerceflow.member.api;

import com.portfolio.commerceflow.common.response.ApiResponse;
import com.portfolio.commerceflow.common.security.JwtAuthenticatedMember;
import com.portfolio.commerceflow.member.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/reissue")
    public ApiResponse<TokenReissueResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
        return ApiResponse.success(authService.reissue(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal JwtAuthenticatedMember authenticatedMember) {
        authService.logout(authenticatedMember.memberId());
        return ApiResponse.successWithoutData();
    }
}
