# 빌드 스테이지
FROM gradle:8.13-jdk21 AS build

# 1. root 권한으로 전환하여 환경 설정
USER root
WORKDIR /app

# 2. 현재 폴더의 모든 파일을 복사하면서 소유권을 gradle 유저로 변경 (중요 ⭐)
COPY --chown=gradle:gradle . .

# 3. Gradle 홈 디렉토리 및 앱 폴더 권한 강제 부여
RUN chown -R gradle:gradle /home/gradle/.gradle /app

# 4. gradle 유저로 전환하여 빌드 실행
USER gradle
# clean을 추가하여 꼬인 캐시를 한 번 비워주는 것이 좋습니다.
RUN gradle clean build --no-daemon -x test

# 실행 스테이지 (이 부분은 기존과 동일하거나 권한 문제가 없다면 그대로 두셔도 됩니다)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# 빌드한 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Python3 및 데이터 분석 라이브러리 설치
RUN apk add --no-cache python3 py3-pip py3-pandas py3-scikit-learn curl

EXPOSE 8090

# JAR 실행
CMD ["java", "-jar", "app.jar"]