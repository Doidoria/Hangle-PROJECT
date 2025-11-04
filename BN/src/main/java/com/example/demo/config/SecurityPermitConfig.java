// com/example/demo/config/SecurityPermitConfig.java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityPermitConfig {

    @Bean
    public SecurityFilterChain openapiPermitAll(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/competition/**",
                                "/api/v1/leaderboard/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
