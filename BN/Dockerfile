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

# # ===== Python & ML 패키지 설치 =====
# RUN apk update && apk add --no-cache \
#     python3 \
#     py3-pip \
#     && pip3 install --no-cache-dir pandas numpy scikit-learn

# # (필요 시) Python 기본 명령어 심볼릭 링크 생성
# RUN ln -s /usr/bin/python3 /usr/bin/python

# ==================================

# 필요 lib 설치
RUN apk add --no-cache curl

# 외부 접근을 위한 포트 설정
EXPOSE 8090

# JAR 실행
CMD ["java", "-jar", "app.jar"]
