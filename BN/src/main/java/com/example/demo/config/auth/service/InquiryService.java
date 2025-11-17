package com.example.demo.config.auth.service;

import com.example.demo.domain.inquiry.dto.InquiryRequestDto;
import com.example.demo.domain.inquiry.dto.InquiryResponseDto;
import com.example.demo.domain.inquiry.entity.Inquiry;
import com.example.demo.domain.inquiry.repository.InquiryRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 ID로 User 엔티티를 조회하고 "사용자 이름(username)"을 반환합니다.
     */
    private String getUserName(Long userId) {
        // User 엔티티에 getUsername() 메서드가 있다고 가정합니다.
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? user.getUsername() : "(탈퇴한 사용자)";
    }

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

        // DTO 생성 시 작성자 이름을 함께 전달
        String userName = getUserName(userId);
        return InquiryResponseDto.of(savedInquiry, userName);
    }

    /**
     * 내 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getMyInquiries(Long userId) {
        // 자신의 문의만 조회하므로, 사용자 이름은 하나로 고정
        String userName = getUserName(userId);

        List<Inquiry> inquiries = inquiryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        // DTO 생성 시 작성자 이름을 함께 전달
        return inquiries.stream()
                .map(inquiry -> InquiryResponseDto.of(inquiry, userName))
                .collect(Collectors.toList());
    }

    /**
     * 문의 삭제 (본인 문의만 가능)
     */
    @Transactional
    public boolean deleteInquiry(Long inquiryId, Long userId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("해당 문의를 찾을 수 없습니다."));

        if (!inquiry.getUserId().equals(userId)) {
            return false;
        }

        inquiryRepository.delete(inquiry);
        return true;
    }

    // =========================================================
    // ↓ 관리자 전용 기능
    // =========================================================

    /**
     * [ADMIN] 전체 문의 목록 조회 (최신 순)
     */
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getAllInquiries() {
        List<Inquiry> allInquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();

        // 문의 리스트를 스트림 처리하면서 사용자 이름을 조회 및 매핑
        return allInquiries.stream()
                .map(inquiry -> {
                    // 각 문의에 대해 userId를 통해 사용자 이름 조회
                    String userName = getUserName(inquiry.getUserId());

                    // 수정된 of() 메서드를 사용해 DTO 생성
                    return InquiryResponseDto.of(inquiry, userName);
                })
                .collect(Collectors.toList());
    }

    /**
     * [ADMIN] 문의 답변 등록/수정
     */
    @Transactional
    public InquiryResponseDto answerInquiry(Long inquiryId, String answerContent) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("해당 문의를 찾을 수 없습니다."));

        inquiry.addAnswer(answerContent);

        // DTO 생성 시 작성자 이름을 함께 전달
        String userName = getUserName(inquiry.getUserId());

        return InquiryResponseDto.of(inquiry, userName);
    }

    /**
     * [ADMIN] 문의 삭제 (관리자용)
     */
    @Transactional
    public void deleteInquiryByAdmin(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("해당 문의를 찾을 수 없습니다."));

        inquiryRepository.delete(inquiry);
    }
}