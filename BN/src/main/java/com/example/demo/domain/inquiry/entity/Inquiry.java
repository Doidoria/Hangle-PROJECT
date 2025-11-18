// File: Inquiry.java
package com.example.demo.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;            // 문의 PK

    @Column(nullable = false)
    private Long userId;        // User PK (id) 저장

    @Column(nullable = false, length = 200)
    private String title;       // 제목

    @Lob
    @Column(nullable = false)
    private String content;     // 내용

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 등록일

    @Lob
    private String answerContent;    // 관리자 답변 내용

    private LocalDateTime answerDate; // 답변 등록/수정 시간
}
