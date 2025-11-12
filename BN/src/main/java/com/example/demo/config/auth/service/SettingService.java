package com.example.demo.config.auth.service;

import com.example.demo.domain.mySetting.entity.Setting;
import com.example.demo.domain.mySetting.repository.SettingRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;


    // ----------------------------------------------------
    // 기본 설정 정보 조회 및 생성 (이메일 변경 전 조회에 사용)
    // ----------------------------------------------------
    @Transactional
    public Setting getOrCreateSetting(String userId) {

        // 1. Setting 정보 조회
        return settingRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 2. Setting이 없으면, User 엔티티를 찾아 기본 설정 정보 생성
                    User user = userRepository.findByUserid(userId);

                    if (user == null) {
                        throw new IllegalArgumentException("User not found with ID: " + userId);
                    }

                    // 3. 기본 설정 정보 생성 및 저장 (기본 이메일은 비워둠)
                    Setting newSetting = Setting.builder()
                            .userId(userId)
                            .user(user)
                            .email(null)
                            .phoneNumber(null)
                            .build();

                    return settingRepository.save(newSetting);
                });
    }

    // ----------------------------------------------------
    // 이메일 변경 로직
    // ----------------------------------------------------
    @Transactional
    public Setting updateEmail(String userId, String newEmail) {

        // 1. 새로운 이메일 중복 확인 (다른 사용자가 이미 사용 중인지 확인)
        settingRepository.findByEmail(newEmail).ifPresent(existingSetting -> {
            if (!existingSetting.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Email is already in use by another user.");
            }
        });

        // 2. 현재 사용자의 Setting 엔티티 조회
        Setting setting = getOrCreateSetting(userId);

        // 3. 이메일 업데이트
        setting.setEmail(newEmail);

        // 4. 저장 (Transactional에 의해 자동 저장되지만 명시적으로 호출)
        return settingRepository.save(setting);
    }

}