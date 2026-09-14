package com.kindtail.adoptmate.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenSessionService tokenSessionService;

    @Value("${client.url:https://paw-mate-frontend.vercel.app}")
    private String clientUrl;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider, TokenSessionService tokenSessionService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenSessionService = tokenSessionService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.createToken(
                userDetails.getId(), userDetails.getEmail(), userDetails.getRole().name(),
                tokenSessionService.tokenVersion(userDetails.getEmail())
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getEmail());

        tokenSessionService.saveRefreshToken(userDetails.getEmail(), refreshToken, jwtTokenProvider.getExpirationRt());

        log.info("OAuth2 Login Success for email: {}", userDetails.getEmail());

        String provider = userDetails.getMember() != null && userDetails.getMember().getSocialProvider() != null
                ? userDetails.getMember().getSocialProvider() : "OAUTH2";

        String html = OAuthResponseUtil.buildPopupSuccessHtml(
                token,
                refreshToken,
                userDetails.getId(),
                userDetails.getRole().name(),
                provider,
                clientUrl
        );

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }

}
