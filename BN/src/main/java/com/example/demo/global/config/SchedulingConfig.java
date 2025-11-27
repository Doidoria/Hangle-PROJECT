package com.example.demo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // @EnableScheduling 덕분에 @Scheduled 가 붙은 빈들이 활성화됩니다.
}
