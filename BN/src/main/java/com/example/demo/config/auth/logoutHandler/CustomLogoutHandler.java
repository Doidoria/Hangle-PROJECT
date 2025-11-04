package com.example.demo.config.auth.logoutHandler;



import com.example.demo.config.auth.PrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomLogoutHandler implements LogoutHandler {


	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

		log.info("CustomLogoutHandler's logout invoke");

		PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal();
		String provider = principalDetails.getUserDto().getProvider();
		if(provider!=null && provider.startsWith("kakao")){

		}else if(provider!=null && provider.startsWith("naver")){

		}else if(provider!=null && provider.startsWith("google")){

		}

		//세션을 새로 생성하지 않음::false
		HttpSession session =  request.getSession(false);
		if(session!=null)
			session.invalidate();

	}

}
