package com.example.demo.domain.leaderboard.service.impl;

import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.entity.Leaderboard;
import com.example.demo.domain.leaderboard.repository.LeaderboardRepository;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
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
                        l.getCompid(),
                        l.getUserid(),
                        l.getSubmissionid(),
                        l.getCompname(),
                        l.getUsername(),
                        l.getScore(),
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
                        l.getCompid(),
                        l.getUserid(),
                        l.getSubmissionid(),
                        l.getCompname(),
                        l.getUsername(),
                        l.getScore(),
                        l.getAttempt(),
                        l.getSubmittedAt(),
                        l.getComprank()
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
            lb.setComprank(currentRank);
        }


        //변경된 rank DB 반영
        leaderboardRepository.saveAll(leaderboardList);
    }
    private Double getCompScore(Leaderboard lb) {
        return (lb.getCompid() != null) ? lb.getScore() : null;
//        return (lb.getComp() != null) ? lb.getSubmit().getBest_score() : null;

    }

    //submit 추가
    public Long leaderBoardAdd(LeaderboardEntryDto dto) throws Exception{


//        User user = userRepository.findById(dto.getUserId())
//                .orElse(null);
//
//        Submit submit = submitRepository.findById(dto.getSubmitId())
//                .orElse(null);
//        Comp comp = compRepository.findById(dto.getCompId())
//              .orElse(null);
//        //submit안에 comp외래키 받은 경우
//        Comp comp = submit.getComp();

        //dto -> entity
        Leaderboard leaderboard = Leaderboard.builder()
                .leaderBoardId(null)
                .compid(dto.getCompid())
                .userid(dto.getUserid())
                .submissionid(dto.getSubmissionid())
                .compname(dto.getCompname())
                .username(dto.getUsername())
                .score(dto.getScore())
                .attempt(dto.getAttempt())
                .submittedAt(dto.getSubmittedAt())
                .comprank(dto.getComprank())
                .build();
        leaderboardRepository.save(leaderboard);

        computeRanksPerComp(dto.getCompid()); //rank 업데이트

        return leaderboard.getLeaderBoardId();
    }

    public Long leaderBoardUpdate(LeaderboardEntryDto dto) throws  Exception{

        //기존 리더보드 가져오기
        Leaderboard list = leaderboardRepository.findById(dto.getLeaderBoardId()).orElse(null);

        if(list == null){
            return null;
        }

        Double oldScore = list.getScore(); //예전 점수
        Double newScore = dto.getScore();

        if(oldScore >= newScore){
            list.setAttempt(dto.getAttempt() + 1);
            list.setSubmittedAt(LocalDateTime.now());
        }else{
            list.setSubmittedAt(LocalDateTime.now());
            list.setAttempt(dto.getAttempt() + 1);
            list.setScore(newScore);
        }

        leaderboardRepository.save(list);

        computeRanksPerComp(list.getCompid());

        return list.getLeaderBoardId();
    }
}
