package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.competition.repository.CSVSave;
import com.example.demo.domain.competition.repository.CompetitionRepository;
import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.entity.Leaderboard;
import com.example.demo.domain.leaderboard.repository.LeaderboardRepository;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CSVSaveServiceExample {

    @Autowired
    private CSVSave csvSave;

    @Autowired
    private LeaderboardService leaderboardService;
    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Transactional
    public Long submit(Long competitionId, Long userId,Long submissionId ,Double score) {

        User user = userRepository.findById(userId).orElseThrow();
        Competition competition = competitionRepository.findById(competitionId).orElseThrow();
        CompetitionCSVSave competitionCSVSave2 = csvSave.findById(submissionId).orElseThrow();

        // 1) 제출 저장
        CompetitionCSVSave competitionCSVSave = CompetitionCSVSave.builder()
                .score(score)
                .build();
        csvSave.save(competitionCSVSave);


        //=============================================================
        //이거 걍 gpt 돌렸어요...
        // 2) 기존 Leaderboard 존재 여부 체크
        Leaderboard lb = leaderboardRepository
                .findByCompetitionIdAndUserId(competitionId, userId)
                .orElse(null);

        // 3) 존재하면 update, 없으면 add
        if (lb == null) {
            // ADD
            leaderboardService.leaderBoardAdd(user, competition, competitionCSVSave2);

        } else {
            // UPDATE
            LeaderboardEntryDto dto = new LeaderboardEntryDto();
            dto.setLeaderBoardId(lb.getLeaderBoardId()); // 어떤 row 업데이트?
            dto.setCsvSave_id(competitionCSVSave.getId());
            dto.setScore(competitionCSVSave.getScore());
            dto.setAttempt(lb.getAttempt()); // DTO에 기존 attempt가 필요하다면 전달

            leaderboardService.leaderBoardUpdate(dto);
        }
        //=============================================================


        return competitionCSVSave.getId();
    }
}
