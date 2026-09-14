package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenSessionService tokenSessionService;
    private final BearerTokenExtractor bearerTokenExtractor;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = bearerTokenExtractor.extract(request);

        if (token != null) {
            if (tokenSessionService.isBlacklisted(token)) {
                log.warn("Attempt to access with blacklisted token");
                request.setAttribute("exception", ErrorCode.LOGOUT_TOKEN);
            } else {
                try {
                    JwtTokenProvider.TokenPrincipal tokenPrincipal = jwtTokenProvider.getTokenPrincipal(token);
                    String email = tokenPrincipal.email();

                    long currentTokenVersion = tokenSessionService.tokenVersion(email);
                    if (jwtTokenProvider.getTokenVersion(token) != currentTokenVersion) {
                        request.setAttribute("exception", ErrorCode.UNAUTHORIZED);
                        filterChain.doFilter(request, response);
                        return;
                    }

                    CustomUserDetails userDetails = new CustomUserDetails(com.kindtail.adoptmate.member.domain.Member.builder()
                            .id(tokenPrincipal.id()).email(email).role(tokenPrincipal.role()).name(email).build());
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (ExpiredJwtException e) {
                    log.warn("Expired JWT token: {}", e.getMessage());
                    request.setAttribute("exception", ErrorCode.UNAUTHORIZED);
                } catch (Exception e) {
                    // Keep the original cause visible.  This catch also covers
                    // user lookup/serialization failures, which are not JWT
                    // parsing failures but previously appeared as the same 401.
                    log.warn("JWT authentication failed for {}: {} ({})",
                            request.getRequestURI(), e.getMessage(), e.getClass().getSimpleName(), e);
                    request.setAttribute("exception", ErrorCode.UNAUTHORIZED);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
