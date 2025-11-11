package com.example.demo.config.auth.Handler;

import java.io.IOException;
import java.util.Arrays;


import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.config.auth.service.PrincipalDetails;
import com.example.demo.domain.user.repository.JwtTokenRepository;
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
        //REDIS 서버에서 REFRESH 토큰 제거
        //----------------------------------
        String userid = null;
        if (authentication != null && authentication.getPrincipal() instanceof PrincipalDetails principal) {
            userid = principal.getUser().getUserid();
        }

//        Cookie useridCookie = new Cookie("userid",null);
//        useridCookie.setMaxAge(0);
//        useridCookie.setPath("/");
//        response.addCookie(useridCookie);
        if (userid != null) {
            redisUtil.delete("RT:" + userid);
            log.info("[LOGOUT] Redis RefreshToken 삭제 완료 → " + userid);
        }

        //-----------------------------------
        //발급받은 ACCESS-TOKEN 쿠키제거
        //-----------------------------------
        clearCookie(response, JwtProperties.ACCESS_TOKEN_COOKIE_NAME);
        clearCookie(response, "userid");
        clearCookie(response, "JSESSIONID"); // 추가: 컨테이너가 다시 세팅 못 하게 즉시 만료

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"Logout success\"}");

		//-----------------------------------
		// TOKEN을 DB에서 삭제
		//-----------------------------------
        String token = null;
        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> JwtProperties.ACCESS_TOKEN_COOKIE_NAME.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        if (token != null) {
            jwtTokenRepository.deleteByAccessToken(token);
        }

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();  // JSESSIONID 만료 Set-Cookie 내려감
        }
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

		//-----------------------------------
		//OAUTH2 SERVER 와 연결 끊기
		//-----------------------------------
		PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal();
		String provider = principalDetails.getUser().getProvider();
		if(provider!=null && provider.startsWith("kakao")){
			response.sendRedirect("https://kauth.kakao.com/oauth/logout?client_id="+KAKAO_CLIENT_ID+"&logout_redirect_uri="+KAKAO_LOGOUT_REDIRECT_URI);
			return ;
		}else if(provider!=null && provider.startsWith("naver")){
			response.sendRedirect("https://nid.naver.com/nidlogin.logout?returl=https://www.naver.com/");
			return ;
		}else if(provider!=null && provider.startsWith("google")){
			response.sendRedirect("https://accounts.google.com/Logout");
			return ;
		}
	}

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");          // 생성 시 Path 동일
        cookie.setHttpOnly(true);     // 생성 시 동일
        cookie.setSecure(false);      // 로컬환경은 false
        cookie.setMaxAge(0);          // 즉시 만료
        response.addHeader("Set-Cookie", name + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        response.addCookie(cookie);
    }

}
