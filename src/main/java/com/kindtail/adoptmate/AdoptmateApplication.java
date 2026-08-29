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

        // 4. 로컬 및 시크릿 환경 파일이 별도 존재하는 경우 추가 로드
        loadDotenvFile(".env.local");
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
