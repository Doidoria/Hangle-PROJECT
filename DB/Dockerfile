# MySQL 8.0 공식 이미지를 기반으로 설정
FROM mysql:8.0

# 환경 변수 설정 (비밀번호는 docker-compose의 DB_PASSWORD로 주입)
ENV MYSQL_DATABASE=hangledb
ENV MYSQL_USER=master

# 3306 포트 노출
EXPOSE 3306 

# MySQL 서버 실행
CMD ["mysqld"]