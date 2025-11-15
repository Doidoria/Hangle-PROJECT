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

        http.securityMatcher("/**");

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/swagger-resources"
                ).permitAll()

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/", "/join", "/login", "/validate", "/oauth2/authorization/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/logout").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/logout").permitAll()

                // 🔥 수정된 정답 — Spring Security 6에서 허용되는 유일한 구조
                .requestMatchers("/api/competitions/*/submit").permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")

                .anyRequest().permitAll()
        );

        // 로그인 직접 처리
        http.formLogin(login -> login.disable());

        // 로그아웃
        http.logout(logout -> {
            logout.permitAll();
            logout.addLogoutHandler(customLogoutHandler);
            logout.logoutSuccessHandler(customLogoutSuccessHandler);
        });

        // 예외처리
        http.exceptionHandling(ex -> {
            ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
            ex.accessDeniedHandler(new CustomAccessDeniedHandler());
        });

        // OAuth2 로그인
        http.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo.userService(principalDetailsOAuth2Service))
                .defaultSuccessUrl("http://localhost:3000/", true)
                .successHandler(oAuth2LoginSuccessHandler())
                .failureUrl("http://localhost:3000/login?error=true")
        );

        // JWT — Stateless
        http.securityContext(context -> context.securityContextRepository(new NullSecurityContextRepository()));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 🔥 submit 요청을 막지 않도록 필터보다 먼저 permitAll 설정하였음
        http.addFilterBefore(jwtAuthorizationFilter, LogoutFilter.class);

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

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

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler oAuth2LoginSuccessHandler() {
        return (request, response, authentication) -> {

            com.example.demo.config.auth.service.PrincipalDetails principalDetails =
                    (com.example.demo.config.auth.service.PrincipalDetails) authentication.getPrincipal();

            String username = principalDetails.getUser().getUsername();
            String userid = principalDetails.getUser().getUserid();

            TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

            redisUtil.setDataExpire(
                    "RT:" + authentication.getName(),
                    tokenInfo.getRefreshToken(),
                    JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000
            );

            ResponseCookie accessCookie = ResponseCookie.from(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, tokenInfo.getAccessToken())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME / 1000)
                    .build();

            ResponseCookie userCookie = ResponseCookie.from("userid", authentication.getName())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000)
                    .build();

            String redirectUrl = "http://localhost:3000/oauth-success?username="
                    + java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8)
                    + "&userid=" + java.net.URLEncoder.encode(userid, java.nio.charset.StandardCharsets.UTF_8);

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, userCookie.toString());

            response.sendRedirect(redirectUrl);
        };
    }
}
