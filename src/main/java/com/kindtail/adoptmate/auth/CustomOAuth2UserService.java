package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.member.domain.AuthProvider;
import com.kindtail.adoptmate.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2MemberPersistenceService oauth2MemberPersistenceService;

    @Override
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

            Map<?, ?> kakaoAccount = asMap(attributes.get("kakao_account"));
            if (kakaoAccount != null) {
                boolean verifiedEmail = Boolean.TRUE.equals(kakaoAccount.get("is_email_verified"));
                email = verifiedEmail ? asString(kakaoAccount.get("email")) : null;

                Map<?, ?> profile = asMap(kakaoAccount.get("profile"));
                if (profile != null) {
                    name = asString(profile.get("nickname"));
                    profileImage = asString(profile.get("profile_image_url"));
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

        AuthProvider authProvider;
        try {
            authProvider = AuthProvider.valueOf(provider.toUpperCase());
        } catch (Exception e) {
            authProvider = AuthProvider.KAKAO;
        }

        final AuthProvider finalAuthProvider = authProvider;
        final String finalEmail = email;
        final String finalName = name;
        final String finalProfileImage = profileImage;
        final String finalSocialId = socialId;

        Member member = oauth2MemberPersistenceService.findOrCreate(
                finalAuthProvider, finalSocialId, finalEmail, finalName, finalProfileImage
        );

        return new CustomUserDetails(member, attributes);
    }

    private Map<?, ?> asMap(Object value) {
        return value instanceof Map<?, ?> map ? map : null;
    }

    private String asString(Object value) {
        return value instanceof String string ? string : null;
    }
}
