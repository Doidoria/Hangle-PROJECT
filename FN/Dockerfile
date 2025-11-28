### 1단계: React 앱 빌드 ###
FROM node:20-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# 환경 변수 파일 명시적으로 COPY (중요!!)
COPY .env.production ./

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

# 커스텀 Nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 포트 노출
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
