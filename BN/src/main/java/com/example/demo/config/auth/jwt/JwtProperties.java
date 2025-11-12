package com.example.demo.config.auth.jwt;

public class JwtProperties {
    public static final int ACCESS_TOKEN_EXPIRATION_TIME = 1000*60*5;
    public static final int REFRESH_TOKEN_EXPIRATION_TIME = 1000*60*60*24*2;       // 10분 (1000*60*60*24) - 1일 ~ 7일
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access-token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";
}