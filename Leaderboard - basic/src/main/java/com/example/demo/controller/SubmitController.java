package com.example.demo.controller;

import com.example.demo.domain.dto.LeaderboardDto;
import com.example.demo.domain.dto.SubmitDto;
import com.example.demo.domain.entity.Comp;
import com.example.demo.domain.entity.Submit;
import com.example.demo.domain.repository.CompRepository;
import com.example.demo.domain.repository.SubmitRepository;
import com.example.demo.domain.service.LeaderboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/submit")
public class SubmitController {

    @Autowired
    private SubmitRepository submitRepository;

    @Autowired
    private CompRepository compRepository;

    public Long compRegisstrarion3(SubmitDto dto) throws Exception{

        //
        Comp comp = compRepository.findById(dto.getCompId())
                .orElseThrow(() -> new IllegalArgumentException("대회 정보 없음. ID=" + dto.getCompId()));


        //dto -> entity
        Submit submit = Submit.builder()
                .submitId(null)
                .best_score(dto.getBest_score())
                .comp(comp) //submit에 comp(대회)를 외래키로 받은 경우
                .build();
        submitRepository.save(submit);
        return submit.getSubmitId();
    }

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping("")
    public void submit_2(){
        log.info("...");
    }
    @PostMapping("")
    public String submit_2(
            SubmitDto dto,
            LeaderboardDto dto2
    ) throws Exception{

        Long submitId = compRegisstrarion3(dto);

        dto2.setSubmitId(submitId);

        if (dto2.getLeaderBoardId() == null) {
            // 새로운 제출 → Add
            leaderboardService.leaderBoardAdd(dto2);
        } else {
            // 기존 제출 → Update
            leaderboardService.leaderBoardUpdate(dto2);
        }
        return "redirect:/leaderboard";
    }


}
