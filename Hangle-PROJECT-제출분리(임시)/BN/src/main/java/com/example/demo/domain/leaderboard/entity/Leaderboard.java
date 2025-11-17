package com.example.demo.domain.leaderboard.entity;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.submmitex.entity.Submission;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "leaderboard")
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaderBoardId;

    @ManyToOne
    @JoinColumn(name="competition_id") //외래키 컬럼명 지정
    private Competition competition; // 대회명, 대회 아이디

    @ManyToOne
    @JoinColumn(name="userid")
    private User user; //유저명, 유저 아이디

    @ManyToOne
    @JoinColumn(name="submission_id")
    private Submission submission;
//    private Long submissionid;
//    private Double score; // 점수

    private Integer attempt; // N번째 제출
    private LocalDateTime submittedAt; //최근 제출 시간

    private Integer comprank;





//    @Id
//    private Long leaderBoardId;
//    private Long compid;
//    private String userid;
//    private Long submissionid;
//    private String compname;
//    private String username;
//    private Double score; // 점수
//    private Integer attempt; // N번째 제출
//    private LocalDateTime submittedAt; //최근 제출 시간
//    private Integer comprank;

}
