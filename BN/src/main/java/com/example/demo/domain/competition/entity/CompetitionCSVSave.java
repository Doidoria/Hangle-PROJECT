package com.example.demo.domain.competition.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionCSVSave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제출 파일 저장 경로
    private String filePath;

    // 제출한 사용자 ID (선택)
    private Long userId;

    // 제출 점수
    private Double score;

    // 어떤 대회에 대한 제출인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id")
    private Competition competition;

}
