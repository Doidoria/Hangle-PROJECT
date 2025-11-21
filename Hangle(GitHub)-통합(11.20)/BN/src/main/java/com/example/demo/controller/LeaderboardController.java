package com.example.demo.controller;

import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
            //검색 결과
            resultList = leaderboardService.searchLeaderboard(keyword);

        }

        // 변경
        List<CompItem> compItem = resultList.stream()
                .map(dto ->new CompItem(dto.getCompetitionId(),dto.getCompetitionTitle()))
                .distinct()
                .toList();
        System.out.println("compItem"+ compItem);

        //변경
        resultList = resultList.stream()
                .sorted(Comparator
                        .comparing(LeaderboardEntryDto::getCompetitionId, Comparator.nullsLast(Long::compareTo))
                        .thenComparing(LeaderboardEntryDto::getComprank, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        System.out.println("leaderboard"+resultList);
        //react에서 여러 데이터 받기
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


