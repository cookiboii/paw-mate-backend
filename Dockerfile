FROM openjdk:17-jdk-slim
WORKDIR /app

# 정확한 jar 경로 지정
COPY build/libs/app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
