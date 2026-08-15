package com.portfolio.commerceflow.member.application;

import com.portfolio.commerceflow.common.exception.BusinessException;
import com.portfolio.commerceflow.common.exception.ErrorCode;
import com.portfolio.commerceflow.member.api.MemberProfileResponse;
import com.portfolio.commerceflow.member.domain.Member;
import com.portfolio.commerceflow.member.infrastructure.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public MemberQueryService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public MemberProfileResponse getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResponse.from(member);
    }
}
