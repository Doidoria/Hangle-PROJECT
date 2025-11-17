package com.example.demo.domain.inquiry.repository;

import com.example.demo.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 마이페이지에서 자신의 문의 목록을 조회하기 위한 메서드 (선택)
    List<Inquiry> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
