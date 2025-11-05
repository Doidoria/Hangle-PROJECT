package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaderBoardId;

    @ManyToOne
    @JoinColumn(name="compId")
    private Comp comp;

    @ManyToOne
    @JoinColumn(name="userId")
    private User user;

    private Double best_score; //최고 점수
    private LocalDateTime last_submit_time; //최근 제출 시간
    private Integer submit_count; // 총 제출 횟수

}
