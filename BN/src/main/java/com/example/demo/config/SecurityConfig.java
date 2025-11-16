package com.example.demo.config;

import com.example.demo.config.auth.jwt.JwtAuthorizationFilter;
import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.Handler.CustomLogoutHandler;
import com.example.demo.config.auth.Handler.CustomLogoutSuccessHandler;
import com.example.demo.config.auth.oauth.PrincipalDetailsOAuth2Service;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.exceptionHandler.CustomAccessDeniedHandler;
import com.example.demo.global.exceptionHandler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
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

        http.securityMatcher("/**");

        /* ===========================
           기본 설정
        =========================== */
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(cs -> cs.disable());

        /* ===========================
           인가 규칙 (중요 순서대로 배치)
        =========================== */
        http.authorizeHttpRequests(auth -> auth

                // 🔥 1) 모든 OPTIONS 요청 허용 (CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 🔥 2) 대회 제출 API 완전 허용 (POST + OPTIONS 모두)
                .requestMatchers("/api/competitions/{id}/submit").permitAll()

                // Swagger
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                        "/swagger-ui.html", "/swagger-resources/**", "/swagger-resources").permitAll()

                // 🔥 로그인/회원가입/validate 같은 public 경로 허용
                .requestMatchers("/", "/join", "/login", "/validate", "/oauth2/authorization/**").permitAll()

                // Logout 허용
                .requestMatchers(HttpMethod.POST, "/logout").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/logout").permitAll()

                // 관리자/매니저 권한
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")

                // 🔥 그 외 모든 요청은 JWT 인증 필요
//                .anyRequest().authenticated()
                .anyRequest().permitAll() // JWT 인증 로직 전체 비활성화

        );

        /* ===========================
           로그인 자체는 우리가 컨트롤러에서 처리
        =========================== */
        http.formLogin(login -> login.disable());

        /* ===========================
           로그아웃
        =========================== */
        http.logout(logout -> {
            logout.permitAll();
            logout.addLogoutHandler(customLogoutHandler);
            logout.logoutSuccessHandler(customLogoutSuccessHandler);
        });

        /* ===========================
           예외 처리
        =========================== */
        http.exceptionHandling(ex -> {
            ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
            ex.accessDeniedHandler(new CustomAccessDeniedHandler());
        });

        /* ===========================
           OAuth2 로그인
        =========================== */
        http.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo.userService(principalDetailsOAuth2Service))
                .defaultSuccessUrl("http://localhost:3000/", true)
                .successHandler(oAuth2LoginSuccessHandler())
                .failureUrl("http://localhost:3000/login?error=true")
        );

        /* ===========================
           JWT Filter (Stateless)
        =========================== */
        http.securityContext(context -> context.securityContextRepository(new NullSecurityContextRepository()));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtAuthorizationFilter, LogoutFilter.class);

        return http.build();
    }

    /* ===========================
       CORS 설정
    =========================== */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(Collections.singletonList("http://localhost:3000"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /* ===========================
       Authentication Manager
    =========================== */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /* ===========================
       OAuth2 Login Success Handler
    =========================== */
    @Bean
    public AuthenticationSuccessHandler oAuth2LoginSuccessHandler() {
        return (request, response, authentication) -> {

            com.example.demo.config.auth.service.PrincipalDetails principalDetails =
                    (com.example.demo.config.auth.service.PrincipalDetails) authentication.getPrincipal();

            String username = principalDetails.getUser().getUsername();
            String userid = principalDetails.getUser().getUserid();

            // JWT 생성
            TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

            // Refresh Redis 저장
            redisUtil.setDataExpire(
                    "RT:" + authentication.getName(),
                    tokenInfo.getRefreshToken(),
                    JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000
            );

            // Access Cookie
            ResponseCookie accessCookie = ResponseCookie.from(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, tokenInfo.getAccessToken())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME / 1000)
                    .build();

            // User Cookie
            ResponseCookie userCookie = ResponseCookie.from("userid", authentication.getName())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000)
                    .build();

            // 클라이언트 이동
            String redirectUrl = "http://localhost:3000/oauth-success?username="
                    + java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8)
                    + "&userid=" + java.net.URLEncoder.encode(userid, java.nio.charset.StandardCharsets.UTF_8);

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, userCookie.toString());

            response.sendRedirect(redirectUrl);
        };
    }
}
