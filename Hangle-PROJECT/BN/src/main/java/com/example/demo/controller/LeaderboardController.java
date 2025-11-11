package com.example.demo.controller;

import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
import com.example.demo.domain.leaderboard.service.LeaderboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        System.out.println("list : " + allList);
        List<LeaderboardEntryDto> resultList;

        //빈 검색 탐색 변수
        boolean isempty = false;

        if( keyword == null || keyword.isEmpty()){
            System.out.println("list2 : " + allList);
            resultList = allList;
            keyword ="";

        }else {
            //검색 결과
            resultList = leaderboardService.searchLeaderboard(keyword);
            System.out.println(resultList.size());
            if (resultList.isEmpty()) {
                isempty = true ;
                resultList =allList;
            }
        }
        //대회명 리스트
        List<String> compNameList = resultList.stream()
                .map(LeaderboardEntryDto::getCompname)
                .distinct()
                .toList();

        //출력용 정렬
        resultList = resultList.stream()
                .sorted(Comparator
                        .comparing(LeaderboardEntryDto::getCompname, Comparator.nullsLast(String::compareTo))
                        .thenComparing(LeaderboardEntryDto::getComprank, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        System.out.println("leaderboard"+resultList);
        //react에서 여러 데이터 받기
        Map<String, Object> response = new HashMap<>();
        response.put("leaderboard", resultList);
        response.put("compNameList", compNameList);
        response.put("keyword", keyword == "" ? "" : keyword);

        if(isempty==true){
            response.put("isEmpty",isempty);
        }

        return ResponseEntity.ok(response);
    }

}