package com.example.demo.config.auth.Handler;

import java.io.IOException;
import java.time.LocalDateTime;
import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.user.entity.JwtToken;
import com.example.demo.domain.user.repository.JwtTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	@Autowired
	private JwtTokenRepository jwtTokenRepository;
	@Autowired
	private RedisUtil redisUtil;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

		log.info("CustomLoginSuccessHandler's onAuthenticationSuccess invoke..");

		//TOKEN 쿠키로 전달
		TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
		Cookie cookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME,tokenInfo.getAccessToken());
		cookie.setMaxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME / 1000);
		cookie.setPath("/");
		response.addCookie(cookie);

		//JWTTOKEN DB 저장
		JwtToken jwtToken = new JwtToken();
		jwtToken.setAccessToken(tokenInfo.getAccessToken());
		jwtToken.setRefreshToken(tokenInfo.getRefreshToken());
		jwtToken.setUserid(authentication.getName());
		jwtToken.setCreatedAt(LocalDateTime.now());
		jwtTokenRepository.save(jwtToken);

		//REDIS 에 REFRESH 저장
		Cookie useridCookie = new Cookie("userid",authentication.getName());
        useridCookie.setMaxAge(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000);
        useridCookie.setPath("/");
		response.addCookie(useridCookie);
        redisUtil.setDataExpire("RT:" + authentication.getName(),
                tokenInfo.getRefreshToken(),
                JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000);

		response.sendRedirect(request.getContextPath()+"/");

	}

}
