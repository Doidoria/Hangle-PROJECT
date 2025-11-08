package com.example.demo.domain.leaderboard.entity;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.Submission;
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

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long leaderBoardId;
//
//    @ManyToOne
//    @JoinColumn(name="compid") //외래키 컬럼명 지정
//    private Competition comp;
//
//    @ManyToOne
//    @JoinColumn(name="userid")
//    private User user;
//
//    @OneToOne
//    @JoinColumn(name="submissionid")
//    private Submission submission;
//    //userid String 임....
//    //최근 제출
//    //n번째 제출 있음
//
//    private Integer rank;

    @Id
    private Long leaderBoardId;
    private Long compid;
    private String userid;
    private Long submissionid;
    private String compname;
    private String username;
    private Double score; // 점수
    private Integer attempt; // N번째 제출
    private LocalDateTime submittedAt; //최근 제출 시간
    private Integer rank;
}
