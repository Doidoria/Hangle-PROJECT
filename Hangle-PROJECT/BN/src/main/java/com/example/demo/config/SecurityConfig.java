package com.example.demo.config;

import com.example.demo.config.auth.jwt.JwtAuthorizationFilter;
import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.Handler.CustomLoginSuccessHandler;
import com.example.demo.config.auth.Handler.CustomLogoutHandler;
import com.example.demo.config.auth.Handler.CustomLogoutSuccessHandler;
import com.example.demo.config.auth.jwt.TokenInfo;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomLoginSuccessHandler customLoginSuccessHandler;
	private final CustomLogoutHandler customLogoutHandler;
	private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisUtil redisUtil;

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(userRepository, jwtTokenProvider, redisUtil);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain openapiPermitAll(HttpSecurity http) throws Exception {
        http.securityMatcher(
                "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                )
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

//    @Bean
//    public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthFilterRegistration(JwtAuthorizationFilter filter) {
//        FilterRegistrationBean<JwtAuthorizationFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(filter);
//        registration.setEnabled(false);
//        return registration;
//    }

	@Bean
    @Order(2)
	protected SecurityFilterChain configure(HttpSecurity http, JwtAuthorizationFilter jwtAuthorizationFilter) throws Exception {
        // CORS 활성화
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		// CSRF비활성화
		http.csrf((config)->{config.disable();});

		//권한체크
        http.securityMatcher("/**");
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            auth.requestMatchers("/", "/join", "/login", "/validate").permitAll();
            auth.requestMatchers(HttpMethod.POST, "/logout").permitAll();
            auth.requestMatchers(HttpMethod.OPTIONS, "/logout").permitAll();
            auth.requestMatchers("/user").hasRole("USER");
            auth.requestMatchers("/manager").hasRole("MANAGER");
            auth.requestMatchers("/admin").hasRole("ADMIN");
            auth.anyRequest().authenticated();
        });
		//-----------------------------------------------------
		// [수정] 로그인(직접처리 - UserRestController)
		//-----------------------------------------------------
		http.formLogin((login)->{
			login.disable();
//            login.permitAll();
//            login.loginPage("/login");
//            login.successHandler(customLoginSuccessHandler());
//            login.failureHandler(new CustomAuthenticationFailureHandler());
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
                .defaultSuccessUrl("http://localhost:3000/", true) // 로그인 성공 후 React 메인 페이지로 리다이렉트
                .successHandler(oAuth2LoginSuccessHandler())
                .failureUrl("http://localhost:3000/login?error=true") // 실패 시 React 로그인 페이지로
        );

		//SESSION INVALIDATED
		http.sessionManagement((sessionManagerConfigure)->{
			sessionManagerConfigure.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		});

		//JWT FILTER ADD
//        http.addFilterBefore(new JwtAuthorizationFilter(userRepository, jwtTokenProvider, redisUtil), LogoutFilter.class);
        http.addFilterBefore(jwtAuthorizationFilter, LogoutFilter.class);
		//-----------------------------------------------
		//[추가] CORS
		//-----------------------------------------------
		http.cors((config)->{
			config.configurationSource(corsConfigurationSource());
		});

		return http.build();

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	//-----------------------------------------------------
	//[추가] CORS
	//-----------------------------------------------------
	@Bean
	CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        // React 개발 서버 주소만 허용
        config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:3000"));
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
            // ✅ 1. JWT 생성
            TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

            // ✅ 2. Redis에 Refresh 저장
            redisUtil.save("RT:" + authentication.getName(), tokenInfo.getRefreshToken());

            // ✅ 3. 쿠키 생성 (Access + User)
            ResponseCookie accessCookie = ResponseCookie.from(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, tokenInfo.getAccessToken())
                    .httpOnly(true)
                    .secure(true) // HTTPS에서만 사용, SameSite=None 대응
                    .sameSite("None")
                    .path("/")
                    .maxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME)
                    .build();

            ResponseCookie userCookie = ResponseCookie.from("userid", authentication.getName())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, userCookie.toString());

            // ✅ 4. 프론트엔드로 리다이렉트
            response.sendRedirect("http://localhost:3000/");
        };
    }

}
