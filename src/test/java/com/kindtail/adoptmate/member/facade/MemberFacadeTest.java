package com.kindtail.adoptmate.member.facade;

import com.kindtail.adoptmate.common.lock.DistributedLockTemplate;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.dto.MemberRegisterRequestDto;
import com.kindtail.adoptmate.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberFacadeTest {

    @Mock
    private DistributedLockTemplate distributedLockTemplate;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberFacade memberFacade;

    @Test
    @DisplayName("회원가입 시 email 기반으로 분산 락 템플릿을 호출한다")
    @SuppressWarnings("unchecked")
    void registerMember_CallsLockTemplate() {
        // given
        String email = "test@pawmate.com";
        MemberRegisterRequestDto request = new MemberRegisterRequestDto("테스터", email, "pwd123", Role.USER);
        Member expectedMember = Member.builder()
                .id(1L)
                .email(email)
                .name("테스터")
                .role(Role.USER)
                .build();

        given(distributedLockTemplate.execute(eq("register:" + email), any(Supplier.class)))
                .willAnswer(invocation -> {
                    Supplier<Member> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        given(memberService.registerMember(request)).willReturn(expectedMember);

        // when
        Member result = memberFacade.registerMember(request);

        // then
        assertThat(result).isEqualTo(expectedMember);
        verify(distributedLockTemplate).execute(eq("register:" + email), any(Supplier.class));
        verify(memberService).registerMember(request);
    }
}
