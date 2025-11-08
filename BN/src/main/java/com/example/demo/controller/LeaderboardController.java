//// com/example/demo/domain/leaderboard/controller/LeaderboardController.java
//package com.example.demo.controller;
//
//import com.example.demo.domain.leaderboard.dto.LeaderboardEntryDto;
//import com.example.demo.domain.leaderboard.service.LeaderboardService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/leaderboard")
//@RequiredArgsConstructor
//@Tag(name = "Leaderboard", description = "리더보드 조회 API")
//public class LeaderboardController {
//
//    private final LeaderboardService leaderboardService;
//
//    @Operation(summary = "리더보드 조회", description = "사용자별 최고 점수 기준 내림차순 리더보드를 반환합니다.")
//    @GetMapping("/{competitionId}")
//    public List<LeaderboardEntryDto> leaderboard(
//            @Parameter(description = "대회 ID") @PathVariable Long competitionId
//    ) {
//        return leaderboardService.leaderboard(competitionId);
//    }
//
//    @Operation(summary = "내 순위 조회", description = "현재 사용자 기준 내 랭크/최고 점수/제출횟수를 반환합니다.")
//    @GetMapping("/{competitionId}/my-rank")
//    public LeaderboardEntryDto myRank(
//            @PathVariable Long competitionId,
//            Principal principal
//    ) {
//        String userid = (principal != null) ? principal.getName() : "anonymous";
//        return leaderboardService.myRank(competitionId, userid);
//    }
//}
