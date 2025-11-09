package com.example.demo.config.auth.service;

import com.example.demo.domain.myProfile.dto.ProfileUpdateRequestDto;
import com.example.demo.domain.myProfile.entity.Profile;
import com.example.demo.domain.myProfile.repository.ProfileRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    // ----------------------------------------------------
    // GET (조회 및 생성) 메서드: UserRepository의 반환값(User) 처리 로직 반영
    // ----------------------------------------------------
    @Transactional
    public Profile getOrCreateProfile(String userId) {

        return profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // UserRepository.findByUserid(String)의 반환값이 User임을 감안하여 null 체크
                    User user = userRepository.findByUserid(userId);

                    if (user == null) {
                        throw new IllegalArgumentException("User not found with ID: " + userId);
                    }

                    Profile newProfile = Profile.builder()
                            .userId(userId)
                            .user(user)
                            .displayName(user.getUsername())
                            .profileImageUrl("default_image_url.png")
                            .build();

                    return profileRepository.save(newProfile);
                });
    }

    // ----------------------------------------------------
    // PUT (업데이트) 메서드
    // ----------------------------------------------------
    @Transactional
    public Profile updateProfile(String userId, ProfileUpdateRequestDto requestDto) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for ID: " + userId));

        if (requestDto.getDisplayName() != null) {
            profile.setDisplayName(requestDto.getDisplayName());
        }

        if (requestDto.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(requestDto.getProfileImageUrl());
        }

        return profileRepository.save(profile);
    }
}