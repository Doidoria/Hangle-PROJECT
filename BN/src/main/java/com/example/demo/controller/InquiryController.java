// src/main/java/com/example/demo/controller/InquiryController.java

package com.example.demo.controller;

import com.example.demo.config.auth.service.InquiryService;
import com.example.demo.config.auth.service.PrincipalDetails;
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
}