package com.kindtail.adoptmate.config;

import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity

public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf( csrf -> csrf.disable() );
        http.cors(Customizer.withDefaults());
        http.sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) );
        http.authorizeHttpRequests( auth->{
            auth
                    .requestMatchers(
                            "/adoptmate/**",
                            "/animals/**",
                            "/adoptions/**",
                            "/post/**",
                            "/comment/**",
                            "/favicon.ico/**",
                            "/h2-console/**"      // H2 콘솔 경로 허용
                    ).permitAll()
                    .anyRequest().authenticated();
        });

        // H2 콘솔을 위한 프레임 옵션 비활성화
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}