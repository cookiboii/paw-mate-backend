package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.member.domain.AuthProvider;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OAuth2MemberPersistenceServiceTest {

    @Mock MemberRepository memberRepository;
    @InjectMocks OAuth2MemberPersistenceService service;

    @Test
    void doesNotAutomaticallyLinkAnExistingEmailAccount() {
        Member existing = Member.builder().email("user@example.com").build();
        given(memberRepository.findByAuthProviderAndSocialId(AuthProvider.KAKAO, "social-id"))
                .willReturn(Optional.empty());
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.findOrCreate(
                AuthProvider.KAKAO, "social-id", "user@example.com", "name", null))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("소셜 계정을 연결");
    }
}
