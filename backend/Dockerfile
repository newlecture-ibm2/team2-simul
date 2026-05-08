FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle 래퍼와 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 의존성 다운로드 (캐시 활용)
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 복사 및 빌드
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 실행 환경
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌더에서 생성된 jar 파일 복사
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
COPY uploads /app/uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
