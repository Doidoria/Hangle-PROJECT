package com.example.demo.domain.competition.entity;

import com.example.demo.domain.competition.entity.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "competitions")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title; // 대회명

    @Column(columnDefinition = "TEXT")
    private String purpose; // 대회 목적 (DTO의 description과 매핑)

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.UPCOMING; // 진행 상태 (UPCOMING, OPEN, CLOSED)

    private LocalDateTime startAt; // 시작일
    private LocalDateTime endAt;   // 종료일

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(length = 40)
    private String evaluationMetric = "ACCURACY"; // 평가 지표 (기본값)

    @Column(precision = 12, scale = 2)
    private BigDecimal prizeTotal; // 총 상금 (null 가능)

    @Column(nullable = false)
    private Integer participantCount = 0; // 참가자 수 (기본 0)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일

    @Column
    private LocalDateTime updatedAt; // 수정일

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
