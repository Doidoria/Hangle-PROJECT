package com.example.demo.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
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
    private Long userId; // 문의 작성자 (UserRestController에서 사용한 User ID를 참조)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status; // 문의 처리 상태 (예: 대기, 완료)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 작성일시

    // 답변 관련 필드는 Answer Entity가 있다면 여기에 @OneToOne 등으로 연결할 수 있습니다.

    @Builder
    public Inquiry(String title, String content, Long userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.status = InquiryStatus.PENDING; // 초기 상태는 '대기'
    }

    public enum InquiryStatus {
        PENDING, // 답변 대기
        ANSWERED // 답변 완료
    }
}
