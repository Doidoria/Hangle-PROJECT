package com.example.demo.domain.leaderboard.service;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.user.entity.User;

import java.util.List;

public interface LeaderboardService {
    public List<LeaderboardEntryDto> getAllLeaderboard();
    public List<LeaderboardEntryDto> searchLeaderboard(String keyword);
    public void computeRanksPerComp(Long compId);
    public Long leaderBoardAdd(User user, Competition competition, CompetitionCSVSave competitionCSVSave);
}
