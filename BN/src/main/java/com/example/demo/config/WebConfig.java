package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 설정된 상대 경로(./uploads/)를 OS에 맞는 절대 경로로 변환
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();

        // 파일 시스템 URI 형식(file:///...)으로 변환
        String uploadPath = path.toUri().toString();

        // /uploads/** 로 들어오는 요청을 실제 폴더로 연결
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
