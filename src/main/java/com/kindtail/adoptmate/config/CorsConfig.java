package com.kindtail.adoptmate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // url 경로설정
                .allowedOrigins("http://localhost:5173") // localhost:5173에서 오는 요청만 허용하겠다.
                .allowedMethods("*") // 요청방식 허용 여부 (GET, POST...)
                .allowedHeaders("*") // 헤더 정보 허용 여부
                .allowCredentials(true); // 인증 정보(JWT)를 포함한 요청을 허용할 것인가
    }
}
