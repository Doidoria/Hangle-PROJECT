package com.example.hangle.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "datasets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 데이터 제목
    private String title;

    // 설명
    private String description;

    // 작성자 이름
    private String author;

    // 좋아요 수
    private int likes;

    // 등록일 (자동 저장)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Entity가 최초 저장될 때 실행 (자동 날짜 입력)
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
