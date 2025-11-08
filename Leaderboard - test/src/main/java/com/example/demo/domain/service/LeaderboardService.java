package com.example.demo.domain.service;

import com.example.demo.domain.dto.CompDto;
import com.example.demo.domain.dto.LeaderboardDto;
import com.example.demo.domain.entity.Comp;
import com.example.demo.domain.entity.Leaderboard;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.CompRepository;
import com.example.demo.domain.repository.LeaderboardRepository;
import com.example.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private CompRepository compRepository;

    @Autowired
    private UserRepository userRepository;


    public List<LeaderboardDto> getAllLeaderboard() {
        System.out.println("LeaderBoardList");
        return leaderboardRepository.findAllOrderByCompThenScore()
                .stream()
                .map(l -> new LeaderboardDto(
                        l.getLeaderBoardId(),
                        l.getComp().getCompId(),
                        l.getUser().getUserId(),
                        l.getComp().getCompName(),
                        l.getUser().getUserName(),
                        l.getBest_score(),
                        l.getLast_submit_time(),
                        l.getSubmit_count()
                ))
                .collect(Collectors.toList());
    }


      public List<LeaderboardDto> searchLeaderboard(String keyword) {
        // 레포지토리에서 다중 조건 검색
        List<Leaderboard> leads = leaderboardRepository.searchAllByKeyword(keyword);
        // 엔티티 → DTO 변환
        return leads.stream()
                .map(l -> new LeaderboardDto(
                        l.getLeaderBoardId(),
                        l.getComp().getCompId(),
                        l.getUser().getUserId(),
                        l.getComp().getCompName(),
                        l.getUser().getUserName(),
                        l.getBest_score(),
                        l.getLast_submit_time(),
                        l.getSubmit_count()
                ))
                .collect(Collectors.toList());
    }


    //다른 컨트롤러에 넣을 것들? 확실한 것은 리더보드에선 추가 안됨

    public Long leaderBoardAdd(LeaderboardDto dto) throws Exception{

        //compName , userName 찾기
        Comp comp = compRepository.findById(dto.getCompId())
                .orElseThrow(() -> new IllegalArgumentException("대회 정보 없음. ID=" + dto.getCompId()));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음. ID=" + dto.getUserId()));

        //dto -> entity
        Leaderboard leaderboard = Leaderboard.builder()
                .leaderBoardId(null)
                .comp(comp)    //Service에서 DTO 변환할 때 꺼내씀
                .user(user)
                .best_score(dto.getBest_score())
                .last_submit_time(LocalDateTime.now())
                .submit_count(1)
                .build();
        leaderboardRepository.save(leaderboard);
        return leaderboard.getLeaderBoardId();
    }

    public Long leaderBoardUpdate(LeaderboardDto dto) throws  Exception{
        //기존 리더보드 조회

        Leaderboard list = leaderboardRepository.findById(dto.getLeaderBoardId()).orElse(null);

        if(list == null){
            return null;
        }

        list.setBest_score(dto.getBest_score());
        list.setLast_submit_time(LocalDateTime.now());
        list.setSubmit_count(dto.getSubmit_count() + 1);

        leaderboardRepository.save(list);

        return list.getLeaderBoardId();
    }

    // 컨트롤러 에서 리더보드 업데이트 도전 list == null 이라면 return 하고 난후 새로 만들기
    //컨트롤러에 넣을 방식
//    if(update == null)
//    create() //add serviece
//    else
//    update()


}
