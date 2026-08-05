package com.kindtail.adoptmate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindtail.adoptmate.auth.CustomOAuth2UserService;
import com.kindtail.adoptmate.auth.CustomUserDetailsService;
import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.OAuth2SuccessHandler;
import com.kindtail.adoptmate.common.dto.CommonErrorDto;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          CustomUserDetailsService customUserDetailsService,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.userDetailsService(customUserDetailsService);

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(
                    "/adoptmate/register",
                    "/adoptmate/login",
                    "/adoptmate/verify-email",
                    "/adoptmate/verify-code",
                    "/adoptmate/send-reset-code",
                    "/adoptmate/verify-reset-code",
                    "/adoptmate/password",
                    "/adoptmate/refresh-token",
                    "/adoptmate/kakao",
                    "/login/oauth2/**",
                    "/oauth2/**",
                    "/favicon.ico/**",
                    "/h2-console/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/uploads/**"
            ).permitAll()
            .requestMatchers(HttpMethod.GET, "/animals/**", "/post/**", "/comment/**").permitAll()
            .anyRequest().authenticated();
        });

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
        );

        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
        );

        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(ErrorCode.UNAUTHORIZED.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            CommonErrorDto errorDto = CommonErrorDto.of(ErrorCode.UNAUTHORIZED);
            response.getWriter().write(objectMapper.writeValueAsString(errorDto));
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(ErrorCode.UNAUTHORIZED_AUTHOR.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            CommonErrorDto errorDto = CommonErrorDto.of(ErrorCode.UNAUTHORIZED_AUTHOR);
            response.getWriter().write(objectMapper.writeValueAsString(errorDto));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}