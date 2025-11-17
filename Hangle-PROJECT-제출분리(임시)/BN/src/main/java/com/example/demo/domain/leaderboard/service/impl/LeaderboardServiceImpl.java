package com.example.demo.domain.leaderboard.service.impl;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.entity.Leaderboard;
import com.example.demo.domain.leaderboard.repository.LeaderboardRepository;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import com.example.demo.domain.submmitex.entity.Submission;
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
                        l.getSubmission().getSubmitId(),
                        l.getSubmission().getScore(),
//                        l.getSubmissionid(),
//                        l.getScore(),
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
                        l.getSubmission().getSubmitId(),
                        l.getSubmission().getScore(),
//                        l.getSubmissionid(),
//                        l.getScore(),
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
        return  lb.getSubmission().getScore();

    }

    //submit => submissionid, score
    //submitservice에 추가 : 예시 버전 submitex에 있음
    public Long leaderBoardAdd(User user, Competition competition, Submission submission){

        //dto -> entity
        Leaderboard leaderboard = Leaderboard.builder()
                .leaderBoardId(null)
                .competition(competition)
                .user(user)
                .submission(submission)
//                .submissionid(dto.getSubmissionid())
//                .score(dto.getScore())
                .attempt(1)
                .submittedAt(LocalDateTime.now())
                .comprank(0) //초반에 comprank업데이트 안되있음 => 0으로 초기화
                .build();
        leaderboardRepository.save(leaderboard);

        computeRanksPerComp(competition.getId()); //rank 업데이트

        return leaderboard.getLeaderBoardId();
    }

    public Long leaderBoardUpdate(LeaderboardEntryDto dto) {

        //기존 리더보드 가져오기
        Leaderboard list = leaderboardRepository.findById(dto.getLeaderBoardId()).orElse(null);

        if(list == null){
            return null;
        }

        Double oldScore = list.getSubmission().getScore(); //예전 점수
        Double newScore = dto.getScore();

        if(oldScore >= newScore){
            list.setAttempt(list.getAttempt() + 1);
            list.setSubmittedAt(LocalDateTime.now());
        }else{
            list.setSubmittedAt(LocalDateTime.now());
            list.setAttempt(list.getAttempt() + 1);
            list.getSubmission().setScore(newScore);
        }

        leaderboardRepository.save(list);

        computeRanksPerComp(list.getCompetition().getId());

        return list.getLeaderBoardId();
    }
}
