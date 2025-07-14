package com.kindtail.adoptmate.common.controller;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.common.service.KakaoOAuthService;
import com.kindtail.adoptmate.member.dto.KakaoUserDto;
import com.kindtail.adoptmate.member.dto.MemberResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/adoptmate")
public class KakaoAuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    public KakaoAuthController(KakaoOAuthService kakaoOAuthService, JwtTokenProvider jwtTokenProvider) {
        this.kakaoOAuthService = kakaoOAuthService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/kakao")
    public void kakaoCallback(String code, HttpServletResponse response)throws IOException {
     String kakaoAccessToken = kakaoOAuthService.getKakaoAccessToken(code);
        KakaoUserDto kakaoUserDto = kakaoOAuthService.getKakaoUser(kakaoAccessToken);
        MemberResponseDto memberResponseDto = kakaoOAuthService.findOrCreateKakaoUser(kakaoUserDto);
        String token = jwtTokenProvider.createToken(memberResponseDto.email(),memberResponseDto.role().toString());
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
                                id: '%s',
                                role: '%s',
                                provider: 'KAKAO'
                            }, 'http://localhost:5173');
                            window.close();
                        } else {
                            window.location.href = 'http://localhost:5173';
                        }
                    </script>
                    <p>카카오 로그인 처리 중...</p>
                </body>
                </html>
                """, token, memberResponseDto.id() , memberResponseDto.role());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);

    }

}
