package com.example.demo.controller;

import com.example.demo.config.auth.service.SettingService;
import com.example.demo.domain.mySetting.dto.EmailUpdateRequestDto;
import com.example.demo.domain.mySetting.entity.Setting;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings")
public class SettingController {

    private final SettingService settingService;

    // SettingResponseDto는 구현하지 않았지만, API 응답으로 사용한다고 가정합니다.

    /**
     * PUT /api/settings/email : 사용자 이메일을 변경합니다.
     */
    @PutMapping("/email")
    public ResponseEntity<?> updateEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EmailUpdateRequestDto requestDto) {

        String userId = userDetails.getUsername();

        // 1. 서비스 호출하여 이메일 변경 및 중복 확인 로직 수행
        Setting updatedSetting = settingService.updateEmail(userId, requestDto.getNewEmail());

        // 2. 응답 (성공 메시지 또는 업데이트된 Setting DTO 반환)
        // 여기서는 간단히 성공 응답만 반환합니다.
        return ResponseEntity.ok().body("Email successfully updated to: " + updatedSetting.getEmail());
    }
}