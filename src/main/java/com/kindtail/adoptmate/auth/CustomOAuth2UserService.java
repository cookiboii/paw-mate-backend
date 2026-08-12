package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase(); // KAKAO, GOOGLE 등
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String socialId = null;
        String email = null;
        String name = null;
        String profileImage = null;

        if ("KAKAO".equalsIgnoreCase(provider)) {
            socialId = String.valueOf(attributes.get("id"));

            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");

                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    name = (String) profile.get("nickname");
                    profileImage = (String) profile.get("profile_image_url");
                }
            }
            if (name == null) {
                name = "카카오사용자_" + socialId;
            }
            if (email == null) {
                email = "kakao_" + socialId + "@social.com";
            }
        }

        log.info("OAuth2 로그인 진행: provider={}, socialId={}, email={}", provider, socialId, email);

        final String finalEmail = email;
        final String finalName = name;
        final String finalProfileImage = profileImage;
        final String finalSocialId = socialId;

        Member member = memberRepository.findBySocialProviderAndSocialId(provider, socialId)
                .orElseGet(() -> {
                    // 이메일로 기존 회원 검색
                    Optional<Member> existingMember = memberRepository.findByEmail(finalEmail);
                    if (existingMember.isPresent()) {
                        return existingMember.get();
                    }
                    // 신규 회원 등록
                    Member newMember = Member.builder()
                            .email(finalEmail)
                            .name(finalName)
                            .profileImage(finalProfileImage)
                            .socialId(finalSocialId)
                            .socialProvider(provider)
                            .role(Role.USER)
                            .build();
                    return memberRepository.save(newMember);
                });

        return new CustomUserDetails(member, attributes);
    }
}
