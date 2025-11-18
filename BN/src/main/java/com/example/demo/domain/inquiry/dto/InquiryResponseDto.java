// File: InquiryResponseDto.java
package com.example.demo.domain.inquiry.dto;

import com.example.demo.domain.inquiry.entity.Inquiry;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InquiryResponseDto {

    private Long id;                // 문의 PK
    private String title;
    private String content;

    private String username;        // 작성자 이름
    private String userid;          // 최신 이메일 (변경 후 것도 여기 반영)
    private LocalDateTime createdAt;

    private String status; // PENDING or ANSWERED

    private String answerContent;
    private LocalDateTime answerDate;

    public static InquiryResponseDto of(
            Inquiry inquiry,
            String username,
            String userid
    ) {
        String status = (inquiry.getAnswerContent() != null && !inquiry.getAnswerContent().isBlank())
                ? "ANSWERED"
                : "PENDING";

        return InquiryResponseDto.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .username(username)
                .userid(userid)
                .createdAt(inquiry.getCreatedAt())
                .answerContent(inquiry.getAnswerContent())
                .answerDate(inquiry.getAnswerDate())
                .status(status)               // ★ 추가
                .build();
    }

}
