package com.example.demo.domain.competition.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long competitionId;
    private String userid;         // 제출자(간단히 String로, 실제로는 FK로 User 연결 권장)
    private double score;          // 계산된 점수
    private int attempt;           // N번째 제출
    private String filePath;       // 업로드된 제출 CSV 경로
    private LocalDateTime submittedAt;
}
