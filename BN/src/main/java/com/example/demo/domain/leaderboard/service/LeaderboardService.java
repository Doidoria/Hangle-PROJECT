package com.example.demo.domain.leaderboard.service;

import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardEntryDto> leaderboard(Long competitionId);
    LeaderboardEntryDto myRank(Long competitionId, String userid);
}
