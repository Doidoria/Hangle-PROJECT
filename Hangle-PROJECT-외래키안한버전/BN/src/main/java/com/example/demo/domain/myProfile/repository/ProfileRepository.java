package com.example.demo.domain.myProfile.repository;

import com.example.demo.domain.myProfile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {

    // UserId를 기반으로 Profile을 조회하는 메서드 (PK가 UserId이므로 findById로 대체 가능하지만 명시적 메서드)
    Optional<Profile> findByUserId(String userId);
}