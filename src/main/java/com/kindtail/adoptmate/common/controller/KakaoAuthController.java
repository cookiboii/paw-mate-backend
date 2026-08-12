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

        String frontendUrl = "https://paw-mate-frontend.vercel.app"; // 실제 프론트엔드 주소로 변경하세요 (또는 @Value로 주입)

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
                """,
                token,                        // 1. token
                refreshToken,                 // 2. refreshToken
                memberResponseDto.id(),       // 3. id
                memberResponseDto.role(),     // 4. role
                frontendUrl,                  // 5. postMessage의 target origin (보안상 '*' 보다는 정확한 URL 권장)
                frontendUrl                   // 6. window.location.href의 리다이렉트 주소
        );

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }
}
