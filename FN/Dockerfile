### 1단계: React 앱 빌드 ###
FROM node:20-alpine AS build

WORKDIR /app

# 1) package.json 먼저 복사
COPY package*.json ./
RUN npm install

# 2) 환경변수 파일을 .env로 복사 (이게 핵심)
ENV NODE_ENV=production
COPY .env.production .env

# 3) 전체 소스 복사
COPY . .

# 4) 빌드
RUN npm run build


### 2단계: Nginx ###
FROM nginx:alpine

COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
