package com.example.demo.config.auth.service;

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

    /**
     * 1:1 문의 작성
     */
    @Transactional
    public InquiryResponseDto createInquiry(InquiryRequestDto requestDto, Long userId) {

        Inquiry inquiry = Inquiry.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .userId(userId)
                .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return InquiryResponseDto.of(savedInquiry);
    }

    /**
     * 내 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getMyInquiries(Long userId) {
        List<Inquiry> inquiries = inquiryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        return inquiries.stream()
                .map(InquiryResponseDto::of)
                .collect(Collectors.toList());
    }

    /**
     * 문의 삭제 (본인 문의만 가능)
     */
    @Transactional
    public boolean deleteInquiry(Long inquiryId, Long userId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("해당 문의를 찾을 수 없습니다."));

        // 본인 확인
        if (!inquiry.getUserId().equals(userId)) {
            return false;
        }

        inquiryRepository.delete(inquiry);
        return true;
    }
}