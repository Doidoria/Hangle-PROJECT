package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                // 전역 JWT 보안 자동 적용
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))

                // JWT Security Scheme 정의
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )

                // UI 정보
                .info(new Info()
                        .title("Hangle API Documentation")
                        .version("v1.0.0")
                        .description("""
                                **Hangle REST API 문서**

                                🔐 인증이 필요한 API는 JWT Bearer 인증이 자동 적용됩니다.  
                                상단의 Authorize 버튼으로 AccessToken을 입력하세요.

                                모든 문서는 ApiDocs.java 한 파일에서만 관리됩니다.
                                """)
                )

                // TAG 자동 등록 → Controller에서 @Tag 안 붙여도 됨
                .tags(List.of(
                        new Tag().name("User").description("사용자 계정 / 인증 / 설정 API"),
                        new Tag().name("Inquiry").description("1:1 문의 API"),
                        new Tag().name("Competition").description("대회 API"),
                        new Tag().name("PortOne").description("본인 인증 API")
                ));
    }
}
