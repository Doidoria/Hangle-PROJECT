package com.example.demo.domain.competition.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionCSVSave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long competitionId;

    private String userid;  // 제출자

    private String fileName;

    private String filePath;

    private LocalDateTime submittedAt;

    private Double score;
}