package com.example.demo.controller;

import com.example.demo.config.auth.service.ProfileService;
import com.example.demo.domain.myProfile.dto.ProfileResponseDto;
import com.example.demo.domain.myProfile.dto.ProfileUpdateRequestDto;
import com.example.demo.domain.myProfile.entity.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * GET /api/profile : 로그인된 사용자의 프로필 조회 (없으면 생성)
     */
    @GetMapping
    public ResponseEntity<ProfileResponseDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails.getUsername();
        Profile profile = profileService.getOrCreateProfile(userId);

        return ResponseEntity.ok(ProfileResponseDto.fromEntity(profile));
    }

    /**
     * PUT /api/profile : 프로필 정보 업데이트 (표시 이름, 이미지 URL)
     */
    @PutMapping
    public ResponseEntity<ProfileResponseDto> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileUpdateRequestDto requestDto) {

        String userId = userDetails.getUsername();

        Profile updatedProfile = profileService.updateProfile(userId, requestDto);

        return ResponseEntity.ok(ProfileResponseDto.fromEntity(updatedProfile));
    }
}