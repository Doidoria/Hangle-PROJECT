package com.example.demo.domain.leaderboard.service.impl;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.entity.Leaderboard;
import com.example.demo.domain.leaderboard.repository.LeaderboardRepository;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
                        l.getCompetition().getId(),
                        l.getCompetition().getTitle(),
                        l.getUser().getId(),
                        l.getUser().getUserid(),
                        l.getUser().getUsername(),
                        l.getCompetitionCSVSave().getId(),
                        l.getCompetitionCSVSave().getScore(),
                        l.getAttempt(),
                        l.getSubmittedAt(),
                        l.getComprank()
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
                        l.getCompetition().getId(),
                        l.getCompetition().getTitle(),
                        l.getUser().getId(),
                        l.getUser().getUserid(),
                        l.getUser().getUsername(),
                        l.getCompetitionCSVSave().getId(),
                        l.getCompetitionCSVSave().getScore(),
                        l.getAttempt(),
                        l.getSubmittedAt(),
                        l.getComprank()
                ))
                .collect(Collectors.toList());
        return list;
    }
    @Override
    public void computeRanksPerComp(Long competitionid) {
        // 해당 대회 참가자만 조회
        List<Leaderboard> leaderboardList = leaderboardRepository.findByCompetition_Id(competitionid);

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
            lb.setComprank(currentRank);
        }


        //변경된 rank DB 반영
        leaderboardRepository.saveAll(leaderboardList);
    }
    private Double getCompScore(Leaderboard lb) {
//      return (lb.getCompetition().getId() != null) ? lb.getScore() : null;
        return  lb.getCompetitionCSVSave().getScore();

    }

    //원본
    public Long leaderBoardAdd(User user, Competition competition, CompetitionCSVSave competitionCSVSave){

        //dto -> entity
        Leaderboard leaderboard = Leaderboard.builder()
                .leaderBoardId(null)
                .competition(competition)
                .user(user)
                .competitionCSVSave(competitionCSVSave)
                .attempt(1)
                .submittedAt(LocalDateTime.now())
                .comprank(0) //초반에 comprank업데이트 안되있음 => 0으로 초기화
                .build();
        leaderboardRepository.save(leaderboard);

        computeRanksPerComp(competition.getId()); //rank 업데이트

        leaderboardRepository.flush(); //flush 함수 추가


        return leaderboard.getLeaderBoardId();
    }

    //변경된 버전
//    public Long leaderBoardAdd(User user, Competition competition, CompetitionCSVSave competitionCSVSave){
//
//        Long competitionId =competition.getId();
//        Long userId = user.getId();
//
//        //기존 리더보드 가져오기
//        Leaderboard lb = leaderboardRepository
//                .findByCompetitionIdAndUserId(competitionId, userId)
//                .orElse(null);
//
//        //기존 리더보드 없을 경우
//        if(lb == null) {
//
//            //dto -> entity
//            lb = Leaderboard.builder()
//                    .leaderBoardId(null)
//                    .competition(competition)
//                    .user(user)
//                    .competitionCSVSave(competitionCSVSave)
//                    .attempt(1)
//                    .submittedAt(LocalDateTime.now())
//                    .comprank(0) //초반에 comprank업데이트 안되있음 => 0으로 초기화
//                    .build();
//            leaderboardRepository.save(lb);
//        } //기존 리더보드 있을 경우
//        else{
//            Double oldScore = lb.getCompetitionCSVSave().getScore(); //예전 점수
//            Double newScore = competitionCSVSave.getScore();
//
//            lb.setAttempt(lb.getAttempt() + 1);
//            lb.setSubmittedAt(LocalDateTime.now());
//
//            if(oldScore < newScore) {
//                lb.getCompetitionCSVSave().setScore(newScore);
//            }
//        }
//
//        computeRanksPerComp(competition.getId()); //rank 업데이트
//        leaderboardRepository.flush(); //flush 함수 추가
//        return lb.getLeaderBoardId();
//    }

}
