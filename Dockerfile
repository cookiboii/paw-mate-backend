# Java 17 기반 이미지 사용
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정 (선택적이지만 권장)
WORKDIR /app

# JAR 파일을 container로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 포트 오픈
EXPOSE 8000

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
