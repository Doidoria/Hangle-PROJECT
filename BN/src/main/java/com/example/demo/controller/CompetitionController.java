// com/example/demo/domain/competition/controller/CompetitionController.java
package com.example.demo.controller;

import com.example.demo.domain.competition.dto.*;
import com.example.demo.domain.competition.service.CompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/competition")
@RequiredArgsConstructor
@Tag(name = "Competition", description = "대회 정보/데이터셋/제출 API")
@Validated
public class CompetitionController {

    private final CompetitionService competitionService;

    @Operation(summary = "대회 목록 조회", description = "전체 대회 리스트를 반환합니다.")
    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @GetMapping
    public List<CompetitionDto> list() {
        return competitionService.listCompetitions();
    }

    @Operation(summary = "대회 상세 조회", description = "특정 대회의 상세 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @GetMapping("/{id}")
    public CompetitionDetailDto detail(
            @Parameter(description = "대회 ID") @PathVariable Long id
    ) {
        return competitionService.getCompetition(id);
    }

    @Operation(
            summary = "데이터셋 다운로드",
            description = "`type`은 `train` 또는 `test` 입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "CSV 다운로드",
                            content = @Content(mediaType = "text/csv"))
            }
    )
    @GetMapping("/{id}/dataset")
    public ResponseEntity<byte[]> download(
            @PathVariable Long id,
            @Parameter(description = "train 또는 test") @RequestParam String type
    ) {
        byte[] bytes = competitionService.downloadDataset(id, type);
        String filename = URLEncoder.encode(type + ".csv", StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN); // csv
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @Operation(
            summary = "예측 결과 제출",
            description = "예측 CSV 파일을 업로드하고 서버에서 점수를 계산해 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            content = @Content(schema = @Schema(implementation = SubmissionResponseDto.class)))
            }
    )
    @PostMapping(path = "/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SubmissionResponseDto submit(
            @PathVariable Long id,
            @Parameter(description = "제출 CSV 파일") @RequestPart("file") MultipartFile file,
            Principal principal
    ) {
        String userid = (principal != null) ? principal.getName() : "anonymous";
        return competitionService.submit(id, userid, file);
    }

    @Operation(summary = "내 제출 이력 조회", description = "현재 사용자 기준 제출 이력을 최신순으로 반환합니다.")
    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @GetMapping("/{id}/submissions")
    public List<SubmissionDto> mySubmissions(
            @PathVariable Long id,
            Principal principal
    ) {
        String userid = (principal != null) ? principal.getName() : "anonymous";
        return competitionService.mySubmissions(id, userid);
    }
}
