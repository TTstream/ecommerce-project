package com.portfolio.commerceflow.member.api;

import com.portfolio.commerceflow.common.response.ApiResponse;
import com.portfolio.commerceflow.common.security.JwtAuthenticatedMember;
import com.portfolio.commerceflow.member.application.MemberQueryService;
import com.portfolio.commerceflow.member.application.MemberSignupService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberSignupService memberSignupService;
    private final MemberQueryService memberQueryService;

    public MemberController(MemberSignupService memberSignupService, MemberQueryService memberQueryService) {
        this.memberSignupService = memberSignupService;
        this.memberQueryService = memberQueryService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberSignupResponse> signup(@Valid @RequestBody MemberSignupRequest request) {
        return ApiResponse.success(memberSignupService.signup(request));
    }

    @GetMapping("/me")
    public ApiResponse<MemberProfileResponse> me(@AuthenticationPrincipal JwtAuthenticatedMember authenticatedMember) {
        return ApiResponse.success(memberQueryService.getProfile(authenticatedMember.memberId()));
    }
}
