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
    private String status;
    private LocalDateTime createdAt;

    // Entity -> DTO 변환을 위한 static 메서드
    public static InquiryResponseDto of(Inquiry inquiry) {
        return InquiryResponseDto.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .status(inquiry.getStatus().name())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
