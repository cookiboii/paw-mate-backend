package com.kindtail.adoptmate.common.controller;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.common.service.KakaoOAuthService;
import com.kindtail.adoptmate.member.dto.KakaoUserDto;
import com.kindtail.adoptmate.member.dto.MemberResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import com.kindtail.adoptmate.member.service.MemberService;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/adoptmate")
public class KakaoAuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    @Value("${client.url}")
    private String clientUrl;

    public KakaoAuthController(KakaoOAuthService kakaoOAuthService, JwtTokenProvider jwtTokenProvider, MemberService memberService) {
        this.kakaoOAuthService = kakaoOAuthService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberService = memberService;
    }

    @GetMapping("/kakao")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        String kakaoAccessToken = kakaoOAuthService.getKakaoAccessToken(code);
        KakaoUserDto kakaoUserDto = kakaoOAuthService.getKakaoUser(kakaoAccessToken);
        MemberResponseDto memberResponseDto = kakaoOAuthService.findOrCreateKakaoUser(kakaoUserDto);
        String token = jwtTokenProvider.createToken(memberResponseDto.email(), memberResponseDto.role().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(memberResponseDto.email());

        memberService.saveRefreshToken(memberResponseDto.email(), refreshToken);

        String html = String.format("""
                <!DOCTYPE html>
                <html>
                <head><title>카카오 로그인 완료</title></head>
                <body>
                    <script>
                        if (window.opener) {
                            window.opener.postMessage({
                                type: 'OAUTH_SUCCESS',
                                token: '%s',
                                refreshToken: '%s',
                                id: '%s',
                                role: '%s',
                                provider: 'KAKAO'
                            }, '%s');
                            window.close();
                        } else {
                            window.location.href = '%s/';
                        }
                    </script>
                    <p>카카오 로그인 처리 중...</p>
                </body>
                </html>
                """, token, refreshToken, memberResponseDto.id(), memberResponseDto.role(), clientUrl, clientUrl);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }
}
