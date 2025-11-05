//// com/example/demo/config/SecurityPermitConfig.java
//package com.example.demo.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//import org.springframework.security.web.util.matcher.OrRequestMatcher;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityPermitConfig {
//
//    @Bean(name = "swaggerPermitAll")
//    @Order(1)
//    public SecurityFilterChain openapiPermitAll(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher(new OrRequestMatcher(
//                        AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
//                        AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
//                        AntPathRequestMatcher.antMatcher("/api/v1/competition/**"),
//                        AntPathRequestMatcher.antMatcher("/api/v1/leaderboard/**")
//                ))
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(reg -> reg
//                        .requestMatchers(
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**",
//                                "/api/v1/competition/**",
//                                "/api/v1/leaderboard/**"
//                        ).permitAll()
//                        // 위 securityMatcher로 범위를 이미 제한했으므로 이 체인에서 잡히는 요청은 전부 허용
//                        .anyRequest().authenticated()
//                        .anyRequest().permitAll()
//                )
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//
//        return http.build();
//    }
//}
