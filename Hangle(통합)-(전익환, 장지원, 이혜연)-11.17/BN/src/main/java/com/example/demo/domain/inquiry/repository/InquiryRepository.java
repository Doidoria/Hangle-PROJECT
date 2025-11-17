package com.example.demo.domain.inquiry.repository;

import com.example.demo.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // =========================================================
    // ↓ 기존 메서드 복구 (userId, createdAt 사용)
    // =========================================================
    /**
     * [USER] 특정 사용자의 문의 목록을 최신 순으로 조회
     */
    List<Inquiry> findAllByUserIdOrderByCreatedAtDesc(Long userId);


    // =========================================================
    // ↓ 관리자 기능 추가 (createdAt 사용)
    // =========================================================
    /**
     * [ADMIN] 모든 문의 목록을 최신 순으로 조회
     */
    List<Inquiry> findAllByOrderByCreatedAtDesc();
}