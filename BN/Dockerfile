# 빌드 스테이지
FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# 실행 스테이지
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# 빌드한 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Python3 및 데이터 분석 라이브러리(pandas, scikit-learn) 설치
# Alpine Linux 패키지 매니저(apk)를 사용합니다.
RUN apk add --no-cache python3 py3-pip py3-pandas py3-scikit-learn

# 필요 lib 설치
RUN apk add --no-cache curl

# 외부 접근을 위한 포트 설정
EXPOSE 8090

# JAR 실행
CMD ["java", "-jar", "app.jar"]
