package com.example.demo.domain.leaderboard.service.impl;

import com.example.demo.domain.competition.entity.Submission;
import com.example.demo.domain.competition.repository.SubmissionRepository;
import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final SubmissionRepository submissionRepository;

    @Override
    public List<LeaderboardEntryDto> leaderboard(Long competitionId) {
        // 사용자별 best score 계산
        List<Submission> subs = submissionRepository.findByCompetitionIdOrderByScoreDesc(competitionId);

        Map<String, List<Submission>> byUser = subs.stream()
                .collect(Collectors.groupingBy(Submission::getUserid));

        List<LeaderboardEntryDto> rows = byUser.entrySet().stream()
                .map(e -> {
                    double best = e.getValue().stream().mapToDouble(Submission::getScore).max().orElse(0.0);
                    return new AbstractMap.SimpleEntry<>(e.getKey(), best);
                })
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue())) // 내림차순
                .map(new java.util.function.Function<AbstractMap.SimpleEntry<String, Double>, LeaderboardEntryDto>() {
                    int rank = 0;
                    double lastScore = Double.NaN;
                    int seen = 0;

                    @Override
                    public LeaderboardEntryDto apply(AbstractMap.SimpleEntry<String, Double> entry) {
                        seen++;
                        double score = entry.getValue();
                        if (Double.compare(score, lastScore) != 0) { // 동점 동일 순위
                            rank = seen;
                            lastScore = score;
                        }
                        // 제출 횟수
                        int submissions = (int) subs.stream().filter(s -> s.getUserid().equals(entry.getKey())).count();
                        return LeaderboardEntryDto.builder()
                                .rank(rank)
                                .userid(entry.getKey())
                                .bestScore(score)
                                .submissions(submissions)
                                .build();
                    }
                }).toList();

        return rows;
    }

    @Override
    public com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto myRank(Long competitionId, String userid) {
        List<com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto> board = leaderboard(competitionId);
        return board.stream()
                .filter(e -> e.getUserid().equals(userid))
                .findFirst()
                .orElse(com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto.builder()
                        .rank(0).userid(userid).bestScore(0).submissions(0).build());
    }
}
