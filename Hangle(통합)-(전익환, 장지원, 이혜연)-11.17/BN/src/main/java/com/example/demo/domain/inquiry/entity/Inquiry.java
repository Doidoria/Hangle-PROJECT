package com.example.demo.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data // @Getter, @Setter 등을 포함하도록 수정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 문의 고유 ID

    @Column(nullable = false, length = 100)
    private String title; // 문의 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 문의 내용

    @Column(nullable = false)
    private Long userId; // 문의 작성자 (User ID를 참조)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status; // 문의 처리 상태 (예: 대기, 완료)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 작성일시

    // =========================================================
    // ↓ 관리자 답변 기능 추가를 위한 필드
    // =========================================================

    @Column(columnDefinition = "TEXT")
    private String answerContent; // 관리자 답변 내용

    private LocalDateTime answerDate; // 답변 완료일 (답변 시점 기록)

    @Builder
    public Inquiry(String title, String content, Long userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.status = InquiryStatus.PENDING; // 초기 상태는 '대기'
    }

    // =========================================================
    // ↓ 비즈니스 로직
    // =========================================================

    // 관리자 답변 등록 메서드
    public void addAnswer(String answerContent) {
        this.answerContent = answerContent;
        this.answerDate = LocalDateTime.now();
        this.status = InquiryStatus.ANSWERED;
    }

    // InquiryStatus Enum은 기존 코드를 따르며, InquiryService와 DTO에서도 이 클래스를 참조합니다.
    public enum InquiryStatus {
        PENDING, // 답변 대기
        ANSWERED // 답변 완료
    }
}