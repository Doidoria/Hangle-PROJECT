package com.example.demo.controller;

import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Leaderboard", description = "리더보드 조회 API")
@CrossOrigin(origins = {"http://127.0.0.1:3000","http://localhost:3000"})
public class LeaderboardController {

    private final LeaderboardService leaderboardService;


    @Operation(summary = "리더보드 조회", description = "사용자별 최고 점수 기준 내림차순 리더보드를 반환합니다.")
    @GetMapping("")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리더보드 조회 성공",
                    content = @Content(schema = @Schema(implementation = LeaderboardEntryDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<Map<String, Object>> LeaderboardAllList(
            @RequestParam(name = "keyword", required = false) String keyword
    )throws Exception{
        log.info("leaderboard list");
        List<LeaderboardEntryDto> allList = leaderboardService.getAllLeaderboard();
        List<LeaderboardEntryDto> resultList;

        //빈 검색 탐색 변수 -> 삭제 처리

        if( keyword == null || keyword.trim().isEmpty()){
            resultList = allList;
            keyword ="";

        }else {
            //검색 결과
            resultList = leaderboardService.searchLeaderboard(keyword);
//
            System.out.println("print : 1");

            // 밑의 조건문 주석 처리
//            if (resultList.isEmpty()) {
//                isempty = true ;
//                System.out.println("print : 2");
//                System.out.println(isempty);
//            }
        }

        // 변경
        List<CompItem> compItem = resultList.stream()
                .map(dto -> new CompItem(dto.getCompetitionId(),dto.getCompetitionTitle()))
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
        response.put("keyword", keyword == "" ? "" : keyword);
//        response.put("isEmpty",isempty); //isempty 주석처리

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


