package com.example.demo.domain.leaderboard.service.impl;

import com.example.demo.domain.competition.entity.Submission;
import com.example.demo.domain.competition.repository.SubmissionRepository;
import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.repository.LeaderboardRepository;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Override
    public List<LeaderboardEntryDto> getAllLeaderboard(){
        System.out.println("LeaderBoardList");
        List<LeaderboardEntryDto> list = leaderboardRepository.findAllOrderByCompThenScore()
                .stream()
                .map(l -> new LeaderboardEntryDto(
                        l.getLeaderBoardId(),
                        l.getCompid(),
                        l.getUserid(),
                        l.getSubmissionid(),
                        l.getCompname(),
                        l.getUsername(),
                        l.getScore(),
                        l.getAttempt(),
                        l.getSubmittedAt(),
                        l.getRank()
                ))
                .collect(Collectors.toList());
        return list;
    }
    @Override
    public List<LeaderboardEntryDto> searchLeaderboard(String keyword){

    }
    @Override
    public void computeRanksPerComp(Long compId){

    }
}
