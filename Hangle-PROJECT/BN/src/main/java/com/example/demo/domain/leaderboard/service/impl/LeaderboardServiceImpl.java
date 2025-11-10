package com.example.demo.domain.leaderboard.service.impl;

import com.example.demo.domain.competition.entity.Submission;
import com.example.demo.domain.competition.repository.SubmissionRepository;
import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.entity.Leaderboard;
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

    private final LeaderboardRepository leaderboardRepository;

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
        // 레포지토리에서 다중 조건 검색
        List<Leaderboard> leads = leaderboardRepository.searchAllByKeyword(keyword);
        // 엔티티 → DTO 변환
        List<LeaderboardEntryDto> list =  leads.stream()
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
    public void computeRanksPerComp(Long compId) {
        // 해당 대회 참가자만 조회
        List<Leaderboard> leaderboardList = leaderboardRepository.findByCompid(compId);

        if (leaderboardList.isEmpty()) return;

        // 점수 내림차순 → 최근 제출 오름차순
        leaderboardList.sort(Comparator
                .comparing((Leaderboard lb) -> getCompScore(lb), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Leaderboard::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder()))
        );
        //순위 계산 (동점자 처리)
        int pos = 0;
        int currentRank = 0;
        Double prevScore = null;

        for (Leaderboard lb : leaderboardList) {
            pos++;
            Double score = getCompScore(lb);
            if (prevScore == null || !Objects.equals(prevScore, score)) {
                currentRank = pos;
                prevScore = score;
            }
            lb.setRank(currentRank);
        }


        //변경된 rank DB 반영
        leaderboardRepository.saveAll(leaderboardList);
    }
    private Double getCompScore(Leaderboard lb) {
        return (lb.getCompid() != null) ? lb.getScore() : null;
//        return (lb.getComp() != null) ? lb.getSubmit().getBest_score() : null;

    }
}
