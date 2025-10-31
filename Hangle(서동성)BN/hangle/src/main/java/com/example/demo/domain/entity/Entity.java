package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@jakarta.persistence.Entity
@Builder
public class Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;          // 데이터셋 제목
    private String category;       // 분야 (예: 교통, 환경 등)
    private int viewCount;         // 조회수
    private int likeCount;         // 좋아요 수
    private String description;    // 간단한 설명
}
