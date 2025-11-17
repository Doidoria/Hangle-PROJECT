package com.example.demo.config;

import com.example.demo.config.auth.jwt.JwtAuthorizationFilter;
import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.Handler.CustomLoginSuccessHandler;
import com.example.demo.config.auth.Handler.CustomLogoutHandler;
import com.example.demo.config.auth.Handler.CustomLogoutSuccessHandler;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.config.auth.oauth.PrincipalDetailsOAuth2Service;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.exceptionHandler.CustomAccessDeniedHandler;
import com.example.demo.global.exceptionHandler.CustomAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity(debug = false)
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomLoginSuccessHandler customLoginSuccessHandler;
	private final CustomLogoutHandler customLogoutHandler;
	private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisUtil redisUtil;
    private final PrincipalDetailsOAuth2Service principalDetailsOAuth2Service;

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(userRepository, jwtTokenProvider, redisUtil);
    }

	@Bean
    @Order(2)
	protected SecurityFilterChain configure(HttpSecurity http, JwtAuthorizationFilter jwtAuthorizationFilter) throws Exception {
        http.securityMatcher("/**"); // 기존 로직 "/**"
        // CORS 활성화
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		// CSRF비활성화
		http.csrf((config)->{config.disable();});

		//권한체크
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(
                    "/uploads/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/swagger-resources"
            ).permitAll();
            auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            auth.requestMatchers("/", "/join", "/login", "/validate", "/oauth2/authorization/**").permitAll();
            auth.requestMatchers("/api/user/me").authenticated();
            auth.requestMatchers(HttpMethod.POST, "/logout").permitAll();
            auth.requestMatchers(HttpMethod.OPTIONS, "/logout").permitAll();

            // =========================================================
            // ↓ 관리자 문의 API 권한 설정 추가
            // =========================================================
            auth.requestMatchers(HttpMethod.GET, "/api/inquiry/admin").hasRole("ADMIN"); // 전체 문의 목록 조회
            auth.requestMatchers(HttpMethod.POST, "/api/inquiry/{id}/answer").hasRole("ADMIN"); // 문의 답변 등록
            auth.requestMatchers(HttpMethod.DELETE, "/api/inquiry/admin/{id}").hasRole("ADMIN"); // 관리자 문의 삭제

            auth.requestMatchers("/user/mypage").hasAnyRole("USER", "ADMIN");
            auth.requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN");
//            auth.anyRequest().hasRole("USER"); // USER 이상만 접근 가능
            auth.anyRequest().permitAll(); // !!임시로 전체 오픈!!
        });

		//-----------------------------------------------------
		// [수정] 로그인(직접처리 - UserRestController)
		//-----------------------------------------------------
		http.formLogin((login)->{
			login.disable();
		});

		//로그아웃
		http.logout((logout)->{
			logout.permitAll();
			logout.addLogoutHandler(customLogoutHandler);
			logout.logoutSuccessHandler(customLogoutSuccessHandler);
		});

		//예외처리
		http.exceptionHandling((ex)->{
			ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
			ex.accessDeniedHandler(new CustomAccessDeniedHandler());
		});

		//OAUTH2-CLIENT
		http.oauth2Login(oauth -> oauth
                .loginPage("/login") // 커스텀 로그인 페이지 유지
                .userInfoEndpoint(userInfo -> userInfo.userService(principalDetailsOAuth2Service))
                .defaultSuccessUrl("http://localhost:3000/", true) // 로그인 성공 후 React 메인 페이지로 리다이렉트
                .successHandler(oAuth2LoginSuccessHandler())
                .failureUrl("http://localhost:3000/login?error=true") // 실패 시 React 로그인 페이지로
        );

		//SESSION INVALIDATED
        http.securityContext(c -> c.securityContextRepository(new NullSecurityContextRepository()));
		http.sessionManagement((sessionManagerConfigure)->{
			sessionManagerConfigure.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		});

        //JWT FILTER ADD
        http.addFilterBefore(jwtAuthorizationFilter, LogoutFilter.class);

		//-----------------------------------------------
		//[추가] CORS
		//-----------------------------------------------
		http.cors((config)->{
			config.configurationSource(corsConfigurationSource());
		});

		return http.build();
	}

	//-----------------------------------------------------
	//[추가] CORS
	//-----------------------------------------------------
	@Bean
	CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        // React 개발 서버 주소만 허용
        config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:3000")); //"http://localhost:5173"
        // 모든 헤더와 메서드 허용
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 쿠키 포함 요청 허용
        config.setAllowCredentials(true);
        // 쿠키 삭제 시 필요한 헤더 노출
        config.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
        // SameSite=None 쿠키를 주고받기 위해 반드시 Secure(false)로 일관성 유지
        // (이건 쿠키 생성/삭제할 때 맞춰줘야 함)
        // 쿠키 생성 시에도 동일하게 secure=false, SameSite=None으로 만들어야 브라우저 인식됨
        // URL 매핑
//        config.setMaxAge(3600L); // 1시간 동안 캐싱 (1시간 동안은 매번 OPTIONS 요청을 다시 보내지 않음)
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
	}

	//-----------------------------------------------------
	//[추가] ATHENTICATION MANAGER 설정 - 로그인 직접처리를 위한 BEAN
	//-----------------------------------------------------
	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

    @Bean
    public AuthenticationSuccessHandler oAuth2LoginSuccessHandler() {
        return (request, response, authentication) -> {

            // PrincipalDetails 캐스팅
            com.example.demo.config.auth.service.PrincipalDetails principalDetails =
                    (com.example.demo.config.auth.service.PrincipalDetails) authentication.getPrincipal();

            // 사용자 이름 가져오기
            String username = principalDetails.getUser().getUsername();
            String userid = principalDetails.getUser().getUserid();

            // 1. JWT 생성
            TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
            // 2. Redis에 Refresh 저장
            redisUtil.setDataExpire("RT:" + authentication.getName(),
                    tokenInfo.getRefreshToken(),
                    JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000);
            // 3. 쿠키 생성 (Access + User)
            ResponseCookie accessCookie = ResponseCookie.from(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, tokenInfo.getAccessToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME / 1000)
                    .build();

            ResponseCookie userCookie = ResponseCookie.from("userid", authentication.getName())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000)
                    .build();

            // React로 username과 함께 리다이렉트
            String redirectUrl = "http://localhost:3000/oauth-success?username="
                    + java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8)
                    + "&userid=" + java.net.URLEncoder.encode(userid, java.nio.charset.StandardCharsets.UTF_8);

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, userCookie.toString());

            response.sendRedirect(redirectUrl);
        };
    }

}
