// src/main/java/com/example/demo/service/InquiryService.java

package com.example.demo.service;

import com.example.demo.domain.inquiry.dto.InquiryRequestDto;
import com.example.demo.domain.inquiry.dto.InquiryResponseDto;
import com.example.demo.domain.inquiry.entity.Inquiry;
import com.example.demo.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    // **[핵심 기능 1] 1:1 문의를 저장하는 비즈니스 로직**
    @Transactional
    public InquiryResponseDto createInquiry(InquiryRequestDto requestDto, Long userId) {

        // 1. DTO를 Entity로 변환
        Inquiry inquiry = Inquiry.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .userId(userId) // Controller에서 전달받은 로그인된 사용자 ID 설정
                .build();

        // 2. Repository를 통해 DB에 저장
        Inquiry savedInquiry = inquiryRepository.save(inquiry);

        // 3. 저장된 Entity를 Response DTO로 변환하여 반환
        return InquiryResponseDto.of(savedInquiry);
    }

    // **[선택 기능] 자신의 문의 목록 조회 (마이페이지에서 사용)**
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getMyInquiries(Long userId) {

        // 1. 사용자 ID로 문의 목록 조회
        List<Inquiry> inquiries = inquiryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        // 2. Entity 리스트를 DTO 리스트로 변환
        return inquiries.stream()
                .map(InquiryResponseDto::of)
                .collect(Collectors.toList());
    }
}