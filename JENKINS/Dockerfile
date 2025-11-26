# 여기에 JENKINS 이미지를 만들어 놓을 것임

FROM jenkins/jenkins:lts

USER root

# 필수 유틸리티 설치
RUN apt-get update && apt-get -y install \
    curl \
    unzip \
    gnupg \
    ca-certificates

# Docker CLI 설치 (공식 스크립트 이용 - docker-ce 포함)
RUN curl -fsSL https://get.docker.com | sh

# AWS CLI 설치
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf awscliv2.zip aws

USER jenkins
