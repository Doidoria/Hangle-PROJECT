package com.example.demo.config.auth.logoutHandler;

import java.io.IOException;
import java.util.Arrays;


import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.repository.JwtTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	String KAKAO_CLIENT_ID;
	@Value("${spring.security.oauth2.client.kakao.logout.redirect.uri}")
	String KAKAO_LOGOUT_REDIRECT_URI;

	@Autowired
	private JwtTokenRepository jwtTokenRepository;

	@Autowired
	private RedisUtil redisUtil;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		log.info("CustomLogoutSuccessHandler onLogoutSuccess invoke.." + authentication);

		//-----------------------------------
		// TOKEN을 DB에서 삭제
		//-----------------------------------
		String token = Arrays.stream(request.getCookies())
						.filter(cookie -> cookie.getName().equals(JwtProperties.ACCESS_TOKEN_COOKIE_NAME)).findFirst()
						.map(cookie -> cookie.getValue())
						.orElse(null);
		jwtTokenRepository.deleteByAccessToken(token);
		//-----------------------------------
		//REDIS 서버에서 REFRESH 토큰 제거
		//----------------------------------
		Cookie usernameCookie = new Cookie("username",null);
		usernameCookie.setMaxAge(0);
		usernameCookie.setPath("/");
		response.addCookie(usernameCookie);
		redisUtil.delete("RT:"+authentication.getName());
		
		//-----------------------------------
		//발급받은 ACCESS-TOKEN 쿠키제거
		//-----------------------------------
		Cookie cookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME,null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);

		//-----------------------------------
		//OAUTH2 SERVER 와 연결 끊기
		//-----------------------------------
		PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal();
		String provider = principalDetails.getUserDto().getProvider();
		if(provider!=null && provider.startsWith("kakao")){
			response.sendRedirect("https://kauth.kakao.com/oauth/logout?client_id="+KAKAO_CLIENT_ID+"&logout_redirect_uri="+KAKAO_LOGOUT_REDIRECT_URI);
			return ;
		}else if(provider!=null && provider.startsWith("naver")){
			//https://nid.naver.com/nidlogin.logout?returl=https://www.naver.com/
			response.sendRedirect("https://nid.naver.com/nidlogin.logout?returl=https://www.naver.com/");
			return ;
		}else if(provider!=null && provider.startsWith("google")){
			response.sendRedirect("https://accounts.google.com/Logout");
			return ;
		}

		response.sendRedirect(request.getContextPath()+"/");
	}

}
