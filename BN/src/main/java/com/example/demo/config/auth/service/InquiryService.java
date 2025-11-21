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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    /**
     * PK(userId)로 User를 찾아서 username / userid 리턴
     */
    private User findUserOrNull(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 1:1 문의 작성
     */
    @Transactional
    public InquiryResponseDto createInquiry(Long userId, InquiryRequestDto requestDto) {
        // PK로 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Inquiry inquiry = Inquiry.builder()
                .userId(user.getId()) // PK 저장
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);

        return InquiryResponseDto.of(
                saved,
                user.getUsername(),
                user.getUserid()
        );
    }

    /**
     * [USER] 내 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getMyInquiries(Long userId) {
        List<Inquiry> list = inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        User user = findUserOrNull(userId);

        String username = user != null ? user.getUsername() : "(탈퇴한 사용자)";
        String userid = user != null ? user.getUserid() : "-";

        return list.stream()
                .map(inq -> InquiryResponseDto.of(inq, username, userid))
                .collect(Collectors.toList());
    }

    /**
     * [USER] 본인 문의 삭제
     */
    @Transactional
    public boolean deleteInquiry(Long inquiryId, Long userId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElse(null);
        if (inquiry == null) return false;

        // 작성자 본인인지 확인
        if (!inquiry.getUserId().equals(userId)) {
            return false;
        }

        inquiryRepository.delete(inquiry);
        return true;
    }

    @Transactional
    public InquiryResponseDto updateInquiry(Long inquiryId, Long userId, InquiryRequestDto dto)
            throws IllegalAccessException {

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("해당 문의를 찾을 수 없습니다."));

        // 작성자 본인인지 확인
        if (!inquiry.getUserId().equals(userId)) {
            throw new IllegalAccessException("본인이 작성한 문의만 수정할 수 있습니다.");
        }

        // 답변이 이미 달렸다면 수정 불가
        if (inquiry.getAnswerContent() != null && !inquiry.getAnswerContent().isBlank()) {
            throw new IllegalStateException("답변 완료된 문의는 수정할 수 없습니다.");
        }

        // 수정
        inquiry.setTitle(dto.getTitle());
        inquiry.setContent(dto.getContent());

        Inquiry saved = inquiryRepository.save(inquiry);

        User user = userRepository.findById(userId).orElse(null);
        String username = user != null ? user.getUsername() : "(탈퇴한 사용자)";
        String userid = user != null ? user.getUserid() : "-";

        return InquiryResponseDto.of(saved, username, userid);
    }


    // =========================================================
    // ↓ 관리자 전용 기능 (Admin)
    // =========================================================

    /**
     * [ADMIN] 전체 문의 목록 조회 (최신 순)
     *  - 여기서 매 건마다 User PK로 조회해서
     *    최신 username / userid 를 반영해 줌
     */
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getAllInquiries() {
        List<Inquiry> allInquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();

        return allInquiries.stream()
                .map(inquiry -> {
                    User user = findUserOrNull(inquiry.getUserId());
                    String username = user != null ? user.getUsername() : "(탈퇴한 사용자)";
                    String userid = user != null ? user.getUserid() : "-";

                    return InquiryResponseDto.of(inquiry, username, userid);
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

        inquiry.setAnswerContent(answerContent);
        inquiry.setAnswerDate(LocalDateTime.now());

        Inquiry saved = inquiryRepository.save(inquiry);

        User user = findUserOrNull(saved.getUserId());
        String username = user != null ? user.getUsername() : "(탈퇴한 사용자)";
        String userid = user != null ? user.getUserid() : "-";

        return InquiryResponseDto.of(saved, username, userid);
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
