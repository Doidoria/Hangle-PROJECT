package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Mini Kaggle API",
                version = "v1",
                description = "Machine Learning 대회 플랫폼 REST API 문서"
        )
)
public class SwaggerConfig { }
