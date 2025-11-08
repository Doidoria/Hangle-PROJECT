package com.example.demo.domain.service;

import com.example.demo.domain.dto.CompDto;
import com.example.demo.domain.dto.LeaderboardDto;
import com.example.demo.domain.entity.Comp;
import com.example.demo.domain.entity.Leaderboard;
import com.example.demo.domain.entity.Submit;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.CompRepository;
import com.example.demo.domain.repository.LeaderboardRepository;
import com.example.demo.domain.repository.SubmitRepository;
import com.example.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private CompRepository compRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubmitRepository submitRepository;


    public List<LeaderboardDto> getAllLeaderboard() {
        System.out.println("LeaderBoardList");
        List<LeaderboardDto> list = leaderboardRepository.findAllOrderByCompThenScore()
                .stream()
                .map(l -> new LeaderboardDto(
                        l.getLeaderBoardId(),
                        l.getComp().getCompId(),
                        l.getUser().getUserId(),
                        l.getSubmit().getSubmitId(),
                        l.getComp().getCompName(),
                        l.getUser().getUserName(),
                        l.getSubmit().getBest_score(),
                        l.getLast_submit_time(),
                        l.getSubmit_count(),
                        l.getRank()
                ))
                .collect(Collectors.toList());
        return list;
    }


      public List<LeaderboardDto> searchLeaderboard(String keyword) {
        // 레포지토리에서 다중 조건 검색
        List<Leaderboard> leads = leaderboardRepository.searchAllByKeyword(keyword);
        // 엔티티 → DTO 변환
          List<LeaderboardDto> list =  leads.stream()
                .map(l -> new LeaderboardDto(
                        l.getLeaderBoardId(),
                        l.getComp().getCompId(),
                        l.getUser().getUserId(),
                        l.getSubmit().getSubmitId(),
                        l.getComp().getCompName(),
                        l.getUser().getUserName(),
                        l.getSubmit().getBest_score(),
                        l.getLast_submit_time(),
                        l.getSubmit_count(),
                        l.getRank()
                ))
                .collect(Collectors.toList());
          return list;
    }


    @Transactional
    public void computeRanksPerComp(Long compId) {
        // 해당 대회 참가자만 조회
        List<Leaderboard> leaderboardList = leaderboardRepository.findByComp_CompId(compId);

        if (leaderboardList.isEmpty()) return;

        // 점수 내림차순 → 최근 제출 오름차순
        leaderboardList.sort(Comparator
                .comparing((Leaderboard lb) -> getCompScore(lb), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Leaderboard::getLast_submit_time, Comparator.nullsLast(Comparator.naturalOrder()))
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
        return (lb.getComp() != null) ? lb.getSubmit().getBest_score() : null;
    }




    //다른 컨트롤러에 넣을 것들? 확실한 것은 리더보드에선 추가 안됨

    public Long leaderBoardAdd(LeaderboardDto dto) throws Exception{


        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음. ID=" + dto.getUserId()));

        Submit submit = submitRepository.findById(dto.getSubmitId())
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음. ID=" + dto.getUserId()));

        //compName , userName 찾기
//        Comp comp = compRepository.findById(dto.getCompId())
//                .orElseThrow(() -> new IllegalArgumentException("대회 정보 없음. ID=" + dto.getCompId()));

        //submit안에 comp외래키 받은 경우
        Comp comp = submit.getComp();

        //dto -> entity
        Leaderboard leaderboard = Leaderboard.builder()
                .leaderBoardId(null)
                .comp(comp)
                .user(user)
                .submit(submit)
                .last_submit_time(LocalDateTime.now())
                .submit_count(1)
                .build();
        leaderboardRepository.save(leaderboard);

        computeRanksPerComp(comp.getCompId()); //rank 업데이트

        return leaderboard.getLeaderBoardId();
    }

    public Long leaderBoardUpdate(LeaderboardDto dto) throws  Exception{

        //기존 리더보드 가져오기
        Leaderboard list = leaderboardRepository.findById(dto.getLeaderBoardId()).orElse(null);

        if(list == null){
            return null;
        }

        Double oldScore = list.getSubmit().getBest_score(); //예전 점수
        Double newScore = dto.getBest_score();

        if(oldScore >= newScore){
            list.setSubmit_count(dto.getSubmit_count() + 1);
            list.setLast_submit_time(LocalDateTime.now());
        }else{
            list.setLast_submit_time(LocalDateTime.now());
            list.setSubmit_count(dto.getSubmit_count() + 1);
            list.getSubmit().setBest_score(newScore);
        }

        leaderboardRepository.save(list);

        computeRanksPerComp(list.getComp().getCompId());

        return list.getLeaderBoardId();
    }



}
