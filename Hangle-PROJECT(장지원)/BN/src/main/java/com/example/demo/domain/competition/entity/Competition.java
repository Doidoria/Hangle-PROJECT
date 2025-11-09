package com.example.demo.domain.competition.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "competition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String prize;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String datasetUrl;
    private String ruleUrl;

    // ✅ 대회 시작/종료일
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // ✅ 시스템 생성/수정 시간 (대회 일정과 별개)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
