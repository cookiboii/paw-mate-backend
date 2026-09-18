package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.member.domain.AuthProvider;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@Service
@RequiredArgsConstructor
public class OAuth2MemberPersistenceService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member findOrCreate(AuthProvider provider, String socialId, String email, String name, String profileImage) {
        Member socialMember = memberRepository.findByAuthProviderAndSocialId(provider, socialId).orElse(null);
        if (socialMember != null) {
            return socialMember;
        }
        if (memberRepository.findByEmail(email).isPresent()) {
            throw accountLinkRequired();
        }
        return memberRepository.save(Member.builder()
                .email(email)
                .name(name)
                .profileImage(profileImage)
                .socialId(socialId)
                .authProvider(provider)
                .role(Role.USER)
                .build());
    }

    private OAuth2AuthenticationException accountLinkRequired() {
        return new OAuth2AuthenticationException(
                new OAuth2Error("account_link_required"),
                "동일한 이메일의 기존 계정이 있습니다. 기존 계정으로 로그인한 뒤 소셜 계정을 연결해주세요."
        );
    }
}
