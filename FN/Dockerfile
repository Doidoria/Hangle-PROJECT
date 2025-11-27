### 1단계: React 앱 빌드 ###
FROM node:20-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# 의존성 설치
COPY package*.json ./
RUN npm install

# 소스 코드 복사 및 빌드
COPY . .
RUN npm run build


### 2단계: Nginx 이미지로 정적 파일 배포 ###
FROM nginx:alpine

# 빌드된 정적 파일을 Nginx 웹 루트로 복사
COPY --from=build /app/build /usr/share/nginx/html

# 🚨 403 Forbidden 최종 해결: Nginx 사용자에게 파일 소유권을 명시적으로 부여 🚨
# 이 명령이 Nginx가 웹 루트(/usr/share/nginx/html)의 모든 파일에 접근할 수 있도록 보장합니다.
RUN chown -R nginx:nginx /usr/share/nginx/html

# (선택) 커스텀 Nginx 설정 파일이 있을 경우 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]