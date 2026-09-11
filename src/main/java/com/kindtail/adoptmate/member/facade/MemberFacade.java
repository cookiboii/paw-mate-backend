package com.kindtail.adoptmate.member.facade;

import com.kindtail.adoptmate.common.lock.DistributedLockTemplate;
import com.kindtail.adoptmate.member.dto.MemberRegisterRequest;
import com.kindtail.adoptmate.member.dto.MemberResponse;
import com.kindtail.adoptmate.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberFacade {

    private final DistributedLockTemplate distributedLockTemplate;
    private final MemberService memberService;

    /**
     * 이메일 기준 분산 락 적용 후 회원 가입
     */
    public MemberResponse registerMember(MemberRegisterRequest request) {
        return distributedLockTemplate.execute(
                "register:" + request.email(),
                () -> memberService.registerMember(request)
        );
    }
}
