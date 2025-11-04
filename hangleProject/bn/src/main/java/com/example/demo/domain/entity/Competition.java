package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
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

    private String status; // OPEN, CLOSED, DRAFT

    private LocalDate startAt;
    private LocalDate endAt;

    private String prize;

    private String datasetUrl;
    private String rulesUrl;
}
