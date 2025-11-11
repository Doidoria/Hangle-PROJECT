package com.example.demo.domain.mySetting.repository;

import com.example.demo.domain.mySetting.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {

    Optional<Setting> findByUserId(String userId);

    // 이메일 중복 확인을 위해 사용됩니다.
    Optional<Setting> findByEmail(String email);
}