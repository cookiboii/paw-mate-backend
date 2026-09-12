package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.member.domain.AuthProvider;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2MemberPersistenceService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member findOrCreate(AuthProvider provider, String socialId, String email, String name, String profileImage) {
        return memberRepository.findByAuthProviderAndSocialId(provider, socialId)
                .orElseGet(() -> memberRepository.findByEmail(email)
                        .map(member -> {
                            member.updateSocialInfo(provider, socialId, profileImage);
                            return member;
                        })
                        .orElseGet(() -> memberRepository.save(Member.builder()
                                .email(email)
                                .name(name)
                                .profileImage(profileImage)
                                .socialId(socialId)
                                .authProvider(provider)
                                .role(Role.USER)
                                .build())));
    }
}
