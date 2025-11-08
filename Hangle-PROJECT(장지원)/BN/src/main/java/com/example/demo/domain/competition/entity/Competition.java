package com.example.demo.domain.competition.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
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

    private LocalDate startDate;
    private LocalDate endDate;
    private String prize;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String datasetUrl;
    private String ruleUrl;

    @CreationTimestamp
    private LocalDateTime startAt;

    @UpdateTimestamp
    private LocalDateTime endAt;
}
