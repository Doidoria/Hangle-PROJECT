package com.example.demo.domain.leaderboard.service;

import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;

import java.util.List;

public interface LeaderboardService {
    public List<LeaderboardEntryDto> getAllLeaderboard();
    public List<LeaderboardEntryDto> searchLeaderboard(String keyword);
    public void computeRanksPerComp(Long compId);
}
