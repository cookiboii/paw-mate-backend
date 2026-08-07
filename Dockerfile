# 1. Build Stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Gradle 스크립트 및 설정 파일 복사 (Docker layer cache 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 미리 다운로드하여 빌드 캐시 최적화
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사 및 빌드 (테스트 제외)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 2. Run Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 보안 강화를 위한 non-root 사용자 설정
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Builder 스테이지에서 생성된 jar 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 노출 포트
EXPOSE 8000

# JVM 실행 옵션 및 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
