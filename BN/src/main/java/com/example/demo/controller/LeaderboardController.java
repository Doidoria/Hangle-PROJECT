package com.example.demo.controller;

import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Slf4j
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> LeaderboardAllList(
            @RequestParam(name = "keyword", required = false) String keyword
    )throws Exception{
        log.info("leaderboard list");
        List<LeaderboardEntryDto> allList = leaderboardService.getAllLeaderboard();
        List<LeaderboardEntryDto> resultList;


        if(keyword == null || keyword.trim().isEmpty()) {
            resultList = allList;
            keyword = "";
        }
        else {
            resultList = leaderboardService.searchLeaderboard(keyword);

        }

        
        List<CompItem> compItem = resultList.stream()
                .map(dto ->new CompItem(dto.getCompetitionId(),dto.getCompetitionTitle()))
                .distinct()
                .toList();
        System.out.println("compItem"+ compItem);

        resultList = resultList.stream()
                .sorted(Comparator
                        .comparing(LeaderboardEntryDto::getCompetitionId, Comparator.nullsLast(Long::compareTo))
                        .thenComparing(LeaderboardEntryDto::getComprank, Comparator.nullsLast(Integer::compareTo)))
                .toList();


        //상위 5개만 출력 //추가
         resultList = resultList.stream()
                 .collect(Collectors.groupingBy(LeaderboardEntryDto::getCompetitionId)) 
                 .values().stream()
                 .map(list -> list.stream().limit(5).toList()) 
                 .flatMap(List::stream) 
                 .toList();

       
        Map<String, Object> response = new HashMap<>();
        response.put("leaderboard", resultList);
        response.put("compItem", compItem);
        response.put("keyword", keyword == null ? "" : keyword);
        return ResponseEntity.ok(response);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompItem{

        private Long id;
        private String title;

    }


}


