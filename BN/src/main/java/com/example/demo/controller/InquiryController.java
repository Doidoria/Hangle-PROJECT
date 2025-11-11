// src/main/java/com/example/demo/controller/InquiryController.java

package com.example.demo.controller;

import com.example.demo.domain.inquiry.dto.InquiryRequestDto;
import com.example.demo.domain.inquiry.dto.InquiryResponseDto;
import com.example.demo.service.InquiryService;
import com.example.demo.config.auth.service.PrincipalDetails;
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



    private Long extractUserId(PrincipalDetails principalDetails) {
        // User 객체가 null인지 확인 (보안 체크)
        if (principalDetails.getUser() == null) {
            throw new RuntimeException("인증되었으나 사용자(User) 객체를 찾을 수 없습니다.");
        }

        return principalDetails.getUser().getId();
    }


    /**
     * POST /api/inquiry : 1:1 문의 작성
     */
    @PostMapping
    public ResponseEntity<InquiryResponseDto> createInquiry(
            @RequestBody InquiryRequestDto requestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = extractUserId(principalDetails);

        // userId가 유효하지 않으면 비정상 응답
        if (userId == null || userId <= 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 UNAUTHORIZED
        }

        InquiryResponseDto response = inquiryService.createInquiry(requestDto, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/inquiry/my : 로그인 사용자가 작성한 문의 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<InquiryResponseDto>> getMyInquiries(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = extractUserId(principalDetails);

        if (userId == null || userId <= 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<InquiryResponseDto> response = inquiryService.getMyInquiries(userId);

        return ResponseEntity.ok(response);
    }
}