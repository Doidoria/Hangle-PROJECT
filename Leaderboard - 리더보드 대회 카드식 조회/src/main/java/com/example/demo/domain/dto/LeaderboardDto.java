package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LeaderboardDto {

    private Long leaderBoardId;
    private Long userId;
    private Long compId;
    private String compName;
    private String userName;
    private Double best_score; //최고 점수
    private LocalDateTime last_submit_time; //최근 제출 시간
    private Integer submit_count; // 총 제출 횟수
}
