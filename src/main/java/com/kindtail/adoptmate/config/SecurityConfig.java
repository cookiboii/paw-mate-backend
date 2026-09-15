package com.kindtail.adoptmate.config;

import com.kindtail.adoptmate.auth.CustomOAuth2UserService;
import com.kindtail.adoptmate.auth.CustomUserDetailsService;
import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final SecurityExceptionHandlers securityExceptionHandlers;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));
        // OAuth2 authorization code flow stores the authorization request (state,
        // redirect URI, etc.) in the HTTP session between the initial redirect and
        // the callback.  STATELESS drops that session and makes the callback fail
        // with an unauthenticated/401 response.  IF_REQUIRED still avoids creating
        // sessions for ordinary JWT requests while allowing the OAuth2 handshake to
        // complete.
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        http.userDetailsService(customUserDetailsService);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.PATCH, "/adoptmate/password").permitAll()
                .requestMatchers("/api/v1/posts/bookmarks/me", "/post/bookmarks/me").authenticated()
                .requestMatchers(
                        "/adoptmate/register",
                        "/adoptmate/login",
                        "/adoptmate/verify-email",
                        "/adoptmate/verify-code",
                        "/adoptmate/send-reset-code",
                        "/adoptmate/verify-reset-code",
                        "/adoptmate/refresh-token",
                        "/adoptmate/kakao",
                        "/login/oauth2/**",
                        "/oauth2/**",
                        "/favicon.ico",
                        "/favicon.ico/**",
                        "/h2-console/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/uploads/**"
                ).permitAll()
                .requestMatchers(
                        HttpMethod.GET,
                        "/animals/**",
                        "/post/**",
                        "/comment/**",
                        "/api/v1/animals/**",
                        "/api/v1/posts/**"
                ).permitAll()
                .anyRequest().authenticated());

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(securityExceptionHandlers.authenticationEntryPoint())
                .accessDeniedHandler(securityExceptionHandlers.accessDeniedHandler())
        );

        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
        );

        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
