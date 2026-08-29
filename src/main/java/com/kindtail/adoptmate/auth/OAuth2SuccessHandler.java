package com.kindtail.adoptmate.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${client.url:https://paw-mate-frontend.vercel.app}")
    private String clientUrl;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.createToken(userDetails.getEmail(), userDetails.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getEmail());

        redisTemplate.opsForValue().set(
                "refreshToken:" + userDetails.getEmail(),
                refreshToken,
                Duration.ofSeconds(jwtTokenProvider.getExpirationRt())
        );

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
