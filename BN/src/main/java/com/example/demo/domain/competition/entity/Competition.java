package com.example.demo.domain.competition.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Competition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;          // 대회명
    @Column(length = 2000)
    private String description;    // 설명
    @Column(length = 1000)
    private String rules;          // 규칙
    private String metric;         // 평가 지표(ex. accuracy, rmse, logloss)
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // 데이터셋 파일 경로(예: S3 키 또는 로컬 경로)
    private String trainPath;
    private String testPath;
    private String groundTruthPath; // 정답(y_true) CSV 경로
}
