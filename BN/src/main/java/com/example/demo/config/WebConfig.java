package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-root}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 절대 경로로 변환
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();

        // URI 문자열로 변환 (file:///app/uploads)
        String uploadPath = path.toUri().toString();

        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }

        // /uploads/** 로 들어오는 요청을 실제 폴더로 연결
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/app/uploads/");
    }
}
