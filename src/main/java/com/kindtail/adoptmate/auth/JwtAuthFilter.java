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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = SecurityUtil.resolveToken(request);

        if (token != null) {
            if (redisTemplate.hasKey("blackList:" + token)) {
                log.warn("Attempt to access with blacklisted token");
                request.setAttribute("exception", ErrorCode.LOGOUT_TOKEN);
            } else {
                try {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (ExpiredJwtException e) {
                    log.warn("Expired JWT token: {}", e.getMessage());
                    request.setAttribute("exception", ErrorCode.UNAUTHORIZED);
                } catch (UsernameNotFoundException | CustomException e) {
                    log.warn("User not found during JWT authentication: {}", e.getMessage());
                    request.setAttribute("exception", ErrorCode.MEMBER_NOT_FOUND);
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
