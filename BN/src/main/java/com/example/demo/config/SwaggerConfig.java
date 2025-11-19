package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // JWT 토큰 인증 스키마 이름
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .info(new Info()
                        .title("Mini Kaggle API Docs")
                        .version("v1.0.0")
                        .description("Mini Kaggle REST API 문서입니다.\n\n" +
                                "JWT 인증이 필요한 API는 🔒 표시가 있습니다.\n" +
                                "로그인(/login) 후 발급된 AccessToken을 Authorize 버튼에 입력하세요.\n" +
                                "예: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                );
    }
}
