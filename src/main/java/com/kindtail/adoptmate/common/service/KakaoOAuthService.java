package com.kindtail.adoptmate.common.service;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.dto.KakaoUserDto;
import com.kindtail.adoptmate.member.dto.MemberResponseDto;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class KakaoOAuthService {

    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${KAKAO_REDIRECT_URI:https://port-0-paw-mate-backend-msiq1pqe2aa00cb9.sel3.cloudtype.app/adoptmate/kakao}")
    private String kakaoRedirectUri;

    @Value("${KAKAO_CLIENT_SECRET:QKusibOT6eZblB1r9klNGruBOgkkQoII}")
    private String kakaoClientSecret;

    public KakaoOAuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String getKakaoAccessToken(String code) {
        String requestUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("client_id", kakaoClientId);
        formData.add("redirect_uri", kakaoRedirectUri);
        formData.add("client_secret", kakaoClientSecret);

        log.info("Requesting Kakao Token with code={}, client_id={}", code, kakaoClientId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(requestUrl, HttpMethod.POST, request, Map.class);

        Map<String, Object> responseJSON = (Map<String, Object>) responseEntity.getBody();
        log.debug("Response from Kakao: {}", responseJSON);
        return (String) responseJSON.get("access_token");
    }

    public KakaoUserDto getKakaoUser(String kakaoAccessToken) {
        String requestUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + kakaoAccessToken);

        ResponseEntity<KakaoUserDto> response = restTemplate.exchange(
                requestUrl, HttpMethod.GET, new HttpEntity<>(headers), KakaoUserDto.class
        );

        return response.getBody();
    }

    @Transactional
    public MemberResponseDto findOrCreateKakaoUser(KakaoUserDto kakaoUser) {
        String socialId = kakaoUser.id().toString();
        String email = (kakaoUser.kakaoAccount() != null && kakaoUser.kakaoAccount().email() != null)
                ? kakaoUser.kakaoAccount().email()
                : "kakao_" + socialId + "@social.com";
        String nickname = (kakaoUser.properties() != null && kakaoUser.properties().nickname() != null)
                ? kakaoUser.properties().nickname()
                : "카카오사용자_" + socialId;
        String profileImage = kakaoUser.properties() != null ? kakaoUser.properties().profileImage() : null;

        Optional<Member> existingUser = memberRepository.findBySocialProviderAndSocialId("KAKAO", socialId);
        if (existingUser.isPresent()) {
            return existingUser.get().toDto();
        }

        Optional<Member> emailUser = memberRepository.findByEmail(email);
        if (emailUser.isPresent()) {
            Member member = emailUser.get();
            member.updateSocialInfo("KAKAO", socialId, profileImage);
            return member.toDto();
        }

        Member member = Member.builder()
                .name(nickname)
                .email(email)
                .profileImage(profileImage)
                .socialId(socialId)
                .socialProvider("KAKAO")
                .role(Role.USER)
                .password(null)
                .build();

        return memberRepository.save(member).toDto();
    }
}
