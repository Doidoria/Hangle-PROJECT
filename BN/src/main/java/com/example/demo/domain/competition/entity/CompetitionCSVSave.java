package com.example.demo.domain.competition.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "competitioncsvsave")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionCSVSave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long competitionId;

    private String userid;        // 제출자 아이디

    private String fileName;      // 원본 파일명
    private String filePath;      // 실제 저장된 파일 경로

    private LocalDateTime submittedAt;  // 제출 시간

    private Double score;         // 채점 결과 점수 (Python 채점 후 저장)
}
