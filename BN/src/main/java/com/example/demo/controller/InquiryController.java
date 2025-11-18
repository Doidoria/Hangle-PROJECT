// File: InquiryController.java
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

    // 로그인 유저의 PK(id) 추출
    private Long extractUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null) {
            throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다.");
        }
        return principalDetails.getUser().getId(); // PK
    }

    /**
     * [USER] 1:1 문의 작성
     * - POST /api/inquiry
     */
    @PostMapping
    public ResponseEntity<InquiryResponseDto> createInquiry(
            @RequestBody InquiryRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long userId = extractUserId(principalDetails);
        InquiryResponseDto response = inquiryService.createInquiry(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * [USER] 내 문의 목록 조회
     * - GET /api/inquiry/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<InquiryResponseDto>> getMyInquiries(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long userId = extractUserId(principalDetails);
        List<InquiryResponseDto> response = inquiryService.getMyInquiries(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * [USER] 내 문의 삭제
     * - DELETE /api/inquiry/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long userId = extractUserId(principalDetails);
        boolean deleted = inquiryService.deleteInquiry(id, userId);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("본인이 작성한 문의만 삭제할 수 있습니다.");
        }

        return ResponseEntity.ok("문의가 삭제되었습니다.");
    }

    // =========================================================
    // ↓ 관리자 전용 (별도 SecurityConfig에서 ROLE_ADMIN 제한)
    // =========================================================

    /**
     * [ADMIN] 전체 문의 목록 조회
     * - GET /api/inquiry/admin
     */
    @GetMapping("/admin")
    public ResponseEntity<List<InquiryResponseDto>> getAllInquiriesForAdmin() {
        List<InquiryResponseDto> response = inquiryService.getAllInquiries();
        return ResponseEntity.ok(response);
    }

    /**
     * [ADMIN] 문의 답변 등록 / 수정
     * - POST /api/inquiry/admin/{id}/answer  (등록)
     * - PUT  /api/inquiry/admin/{id}/answer  (수정)
     */
    @PostMapping("/admin/{id}/answer")
    public ResponseEntity<InquiryResponseDto> answerInquiry(
            @PathVariable("id") Long inquiryId,
            @RequestBody InquiryAnswerRequestDto requestDto
    ) {
        InquiryResponseDto response =
                inquiryService.answerInquiry(inquiryId, requestDto.getAnswerContent());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/{id}/answer")
    public ResponseEntity<InquiryResponseDto> updateAnswer(
            @PathVariable("id") Long inquiryId,
            @RequestBody InquiryAnswerRequestDto requestDto
    ) {
        InquiryResponseDto response =
                inquiryService.answerInquiry(inquiryId, requestDto.getAnswerContent());
        return ResponseEntity.ok(response);
    }

    /**
     * [ADMIN] 문의 삭제
     * - DELETE /api/inquiry/admin/{id}
     */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteInquiryByAdmin(@PathVariable("id") Long inquiryId) {
        inquiryService.deleteInquiryByAdmin(inquiryId);
        return ResponseEntity.ok("문의가 삭제되었습니다. (관리자)");
    }
}
