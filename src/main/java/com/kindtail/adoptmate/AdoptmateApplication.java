package com.kindtail.adoptmate;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class AdoptmateApplication {

    public static void main(String[] args) {
        loadEnvironmentVariables();
        SpringApplication.run(AdoptmateApplication.class, args);
    }

    private static void loadEnvironmentVariables() {
        // 1. 기본 .env 파일 로드 (공통 설정)
        loadDotenvFile(".env");

        // 2. 활성 프로파일 확인
        String activeProfile = System.getProperty("SPRING_PROFILES_ACTIVE",
                System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "local"));

        // 3. 프로파일별 .env 파일 로드 (예: .env.local, .env.prod, .env.dev 등)
        if (activeProfile != null && !activeProfile.isBlank()) {
            loadDotenvFile(".env." + activeProfile.trim().toLowerCase());
        }

        // Do not load the local profile after the active profile.  Loading it
        // unconditionally overwrote the production OAuth redirect URI with
        // http://localhost:8000, so Kakao sent the authorization code to the
        // user's machine and the backend could not issue a JWT.
        if ("local".equalsIgnoreCase(activeProfile)) {
            loadDotenvFile(".env.local");
        }

        // Local secrets are optional and are loaded last for either profile.
        loadDotenvFile(".env.secret");
    }

    private static void loadDotenvFile(String filename) {
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            try {
                Dotenv dotenv = Dotenv.configure()
                        .filename(filename)
                        .ignoreIfMissing()
                        .load();
                dotenv.entries().forEach(entry -> {
                    // 환경변수 값이 비어있지 않은 경우 System Property로 설정
                    if (entry.getValue() != null && !entry.getValue().isBlank()) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
            } catch (Exception ignored) {
            }
        }
    }
}
