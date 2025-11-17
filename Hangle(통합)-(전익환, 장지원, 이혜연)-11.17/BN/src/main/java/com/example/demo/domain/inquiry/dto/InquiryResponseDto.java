package com.example.demo.domain.inquiry.dto;

import com.example.demo.domain.inquiry.entity.Inquiry;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryResponseDto {

    private Long id;
    private String title;
    private String content;

    private Long userId;

    private String userName;

    private String status;
    private LocalDateTime createdAt;

    private String answerContent;
    private LocalDateTime answerDate;

    /**
     * Entity와 사용자 로그인 ID를 매개변수로 받아 DTO를 생성하는 팩토리 메서드
     */
    public static InquiryResponseDto of(Inquiry inquiry, String userName) {
        return InquiryResponseDto.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .userId(inquiry.getUserId())

                .userName(userName) // 실제 사용자 이름 설정

                .status(inquiry.getStatus().name())
                .createdAt(inquiry.getCreatedAt())

                .answerContent(inquiry.getAnswerContent())
                .answerDate(inquiry.getAnswerDate())
                .build();
    }
}