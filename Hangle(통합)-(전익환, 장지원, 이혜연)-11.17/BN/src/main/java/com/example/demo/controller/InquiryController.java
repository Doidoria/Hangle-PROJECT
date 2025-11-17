package com.example.demo.controller;

import com.example.demo.config.auth.service.InquiryService;
import com.example.demo.config.auth.service.PrincipalDetails;
import com.example.demo.domain.inquiry.dto.InquiryAnswerRequestDto;
import com.example.demo.domain.inquiry.dto.InquiryRequestDto;
import com.example.demo.domain.inquiry.dto.InquiryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 로그인 유저 ID 추출
    private Long extractUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null) {
            throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다.");
        }
        return principalDetails.getUser().getId();
    }

    /**
     * 1:1 문의 작성
     */
    @PostMapping
    public ResponseEntity<InquiryResponseDto> createInquiry(
            @RequestBody InquiryRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = extractUserId(principalDetails);

        InquiryResponseDto response = inquiryService.createInquiry(requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 나의 문의 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<InquiryResponseDto>> getMyInquiries(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = extractUserId(principalDetails);

        List<InquiryResponseDto> response = inquiryService.getMyInquiries(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 문의 삭제 (본인 문의만 가능)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = extractUserId(principalDetails);
        boolean deleted = inquiryService.deleteInquiry(id, userId);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("본인이 작성한 문의만 삭제할 수 있습니다.");
        }

        return ResponseEntity.ok().build();
    }

    // =========================================================
    // ↓ 관리자 전용 기능 (Admin)
    // =========================================================

    /**
     * [ADMIN] 전체 문의 목록 조회
     */
    @GetMapping("/admin")
    public ResponseEntity<List<InquiryResponseDto>> getAllInquiriesForAdmin() {
        List<InquiryResponseDto> response = inquiryService.getAllInquiries();
        return ResponseEntity.ok(response);
    }

    /**
     * [ADMIN] 문의 답변 등록
     */
    @PostMapping("/{id}/answer")
    public ResponseEntity<InquiryResponseDto> answerInquiry(
            @PathVariable("id") Long inquiryId,
            @RequestBody InquiryAnswerRequestDto requestDto) {

        String answerContent = requestDto.getAnswerContent();

        InquiryResponseDto response = inquiryService.answerInquiry(inquiryId, answerContent);
        return ResponseEntity.ok(response);
    }

    /**
     * [ADMIN] 문의 삭제
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteInquiryByAdmin(@PathVariable("id") Long inquiryId) {

        inquiryService.deleteInquiryByAdmin(inquiryId);
        return ResponseEntity.ok().build();
    }
}