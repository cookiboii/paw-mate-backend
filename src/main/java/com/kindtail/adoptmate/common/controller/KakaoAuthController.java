package com.kindtail.adoptmate.common.controller;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.auth.OAuthResponseUtil;
import com.kindtail.adoptmate.common.service.KakaoOAuthService;
import com.kindtail.adoptmate.member.dto.KakaoUserDto;
import com.kindtail.adoptmate.member.dto.MemberResponseDto;
import com.kindtail.adoptmate.member.service.MemberService;
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
    private final MemberService memberService;

    @Value("${client.url:https://paw-mate-frontend.vercel.app}")
    private String clientUrl;

    public KakaoAuthController(KakaoOAuthService kakaoOAuthService, JwtTokenProvider jwtTokenProvider, MemberService memberService) {
        this.kakaoOAuthService = kakaoOAuthService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberService = memberService;
    }

    @Override
    @GetMapping("/kakao")
    public void kakaoCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        String kakaoAccessToken = kakaoOAuthService.getKakaoAccessToken(code);
        KakaoUserDto kakaoUserDto = kakaoOAuthService.getKakaoUser(kakaoAccessToken);
        MemberResponseDto memberResponseDto = kakaoOAuthService.findOrCreateKakaoUser(kakaoUserDto);
        String token = jwtTokenProvider.createToken(memberResponseDto.email(), memberResponseDto.role().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(memberResponseDto.email());

        memberService.saveRefreshToken(memberResponseDto.email(), refreshToken);

        String html = OAuthResponseUtil.buildPopupSuccessHtml(
                token,
                refreshToken,
                memberResponseDto.id(),
                memberResponseDto.role().toString(),
                "KAKAO",
                this.clientUrl
        );

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }
}
