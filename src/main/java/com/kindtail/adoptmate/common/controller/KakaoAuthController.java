package com.kindtail.adoptmate.common.controller;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.auth.TokenSessionService;
import com.kindtail.adoptmate.auth.AuthenticationService;
import com.kindtail.adoptmate.auth.OAuthResponseUtil;
import com.kindtail.adoptmate.common.service.KakaoOAuthService;
import com.kindtail.adoptmate.member.dto.KakaoUserResponse;
import com.kindtail.adoptmate.member.dto.MemberResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/adoptmate")
public class KakaoAuthController implements KakaoAuthControllerDocs {

    private final KakaoOAuthService kakaoOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationService authenticationService;
    private final TokenSessionService tokenSessionService;

    @Value("${client.url:https://paw-mate-frontend.vercel.app}")
    private String clientUrl;

    public KakaoAuthController(KakaoOAuthService kakaoOAuthService, JwtTokenProvider jwtTokenProvider, AuthenticationService authenticationService, TokenSessionService tokenSessionService) {
        this.kakaoOAuthService = kakaoOAuthService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationService = authenticationService;
        this.tokenSessionService = tokenSessionService;
    }

    @Override
    @GetMapping("/kakao")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        String kakaoAccessToken = kakaoOAuthService.getKakaoAccessToken(code);
        KakaoUserResponse kakaoUser = kakaoOAuthService.getKakaoUser(kakaoAccessToken);
        MemberResponse memberResponse = kakaoOAuthService.findOrCreateKakaoUser(kakaoUser);
        String token = jwtTokenProvider.createToken(
                memberResponse.id(), memberResponse.email(), memberResponse.role().toString(),
                tokenSessionService.tokenVersion(memberResponse.email())
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(memberResponse.email());

        authenticationService.saveRefreshToken(memberResponse.email(), refreshToken);

        String html = OAuthResponseUtil.buildPopupSuccessHtml(
                token,
                refreshToken,
                memberResponse.id(),
                memberResponse.role().toString(),
                "KAKAO",
                this.clientUrl
        );

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }
}
