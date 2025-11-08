package com.example.demo.controller;

import com.example.demo.domain.dto.LeaderboardDto;
import com.example.demo.domain.entity.Leaderboard;
import com.example.demo.domain.repository.LeaderboardRepository;
import com.example.demo.domain.service.LeaderboardService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

//@RestController
@Controller
@RequestMapping("/leaderboard")
@Slf4j
//@CrossOrigin(origins = {"http://127.0.0.1:3000","http://localhost:3000"})
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @GetMapping("")
    public String LeaderboardAllList(
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model
    )throws Exception{
        log.info("leaderboard list");
        List<LeaderboardDto> allList = leaderboardService.getAllLeaderboard();
        System.out.println("list : " + allList);
        List<LeaderboardDto> resultList;


        if( keyword == null || keyword.isEmpty()){
            System.out.println("list2 : " + allList);
            resultList = allList;
            keyword =null;

        }else {
            //검색 결과
            resultList = leaderboardService.searchLeaderboard(keyword);
            System.out.println(resultList.size());
            if (resultList.isEmpty()) {
                model.addAttribute("errorMessage", "검색 결과가 없습니다.");
               resultList =allList;
            }
        }
        //대회명 리스트
        List<String> compNameList = resultList.stream()
                .map(LeaderboardDto::getCompName)
                .distinct()
                .toList();

        //출력용 정렬
        resultList = resultList.stream()
                .sorted(Comparator
                        .comparing(LeaderboardDto::getCompName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(LeaderboardDto::getRank, Comparator.nullsLast(Integer::compareTo)))
                .toList();


        model.addAttribute("leaderboard",resultList );
        model.addAttribute("compNameList",compNameList );
        model.addAttribute("keyword",keyword==null ? "":keyword);
        System.out.println("leaderboard"+resultList);
        return "leaderboard";
    }



}