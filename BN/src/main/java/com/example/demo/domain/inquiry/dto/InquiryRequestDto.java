package com.example.demo.domain.inquiry.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryRequestDto {

    private String title;
    private String content;

    // Getter, Setter, Constructors는 Lombok의 @Getter, @NoArgsConstructor로 처리
}