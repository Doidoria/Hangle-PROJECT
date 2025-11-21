package com.example.demo.controller;

import com.example.demo.config.auth.service.PrincipalDetails;
import com.example.demo.domain.competition.dtos.CompetitionCreateRequest;
import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.dtos.CompetitionUpdateRequest;
import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.repository.CompetitionCSVSaveRepository;
import com.example.demo.domain.competition.service.CSVSaveService;
import com.example.demo.domain.competition.service.CompetitionService;
import com.example.demo.domain.competition.service.ScoreService;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.AppUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;
//    private final CompetitionService service;
    private final CSVSaveService csvSaveService;
    private final UserRepository userRepository;
    private final AppUserService appUserService;
    private final LeaderboardService leaderboardService;
    private final CompetitionCSVSaveRepository csvSaveRepository;
    private final ScoreService scoreService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public Page<CompetitionDto> getAll(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return competitionService.search(status, keyword, page, size, sort);
    }

    @GetMapping("/{id}")
    public CompetitionDto getById(@PathVariable Long id) {
        return competitionService.get(id);
    }

    // JSON only 방식 -> multipart 통합으로 사용 x 주석 처리 해둠
//    @PostMapping
//    public ResponseEntity<CompetitionDto> create(@Valid @RequestBody CompetitionCreateRequest req) {
//        CompetitionDto created = service.create(req);
//        URI location = URI.create("/api/competitions/" + created.id());
//        return ResponseEntity.created(location).body(created);
//    }

    @PutMapping("/{id}")
    public CompetitionDto update(@PathVariable Long id, @Valid @RequestBody CompetitionUpdateRequest req) {
        return competitionService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        competitionService.delete(id);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<CompetitionDto> create(
            @RequestPart("request") String requestJson,
            @RequestPart("trainFile") MultipartFile trainFile,
            @RequestPart("testFile") MultipartFile testFile,
            @RequestPart(value = "customScoreFile", required = false) MultipartFile customScoreFile
    ) throws JsonProcessingException {
        CompetitionCreateRequest request =
                objectMapper.readValue(requestJson, CompetitionCreateRequest.class);

        CompetitionDto created = competitionService.createWithFiles(request, trainFile, testFile, customScoreFile);

        URI location = URI.create("/api/competitions/" + created.id());
        return ResponseEntity.created(location).body(created);
//        return ResponseEntity.ok(created);
    }


    /** ======================================================
     *  🔥🔥 CSV 제출 API
     * ====================================================== */
    @PostMapping("/{competitionId}/submit")
    public ResponseEntity<?> submit(
            @PathVariable Long competitionId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        String userid = principalDetails.getUser().getUserid();
        // 1) 유저 조회
        User user = appUserService.findByUserid(userid);
        if (user == null) {
            return ResponseEntity.badRequest().body("INVALID_USER");
        }

        // 2) 대회 조회
        Competition competition = competitionService.findEntity(competitionId);
        if (competition == null) {
            return ResponseEntity.badRequest().body("INVALID_COMPETITION");
        }

        // 3) CSV 저장
        CompetitionCSVSave save = csvSaveService.saveCSV(file, user, competition);

        // 4) Leaderboard 기록 생성
        leaderboardService.leaderBoardAdd(user, competition, save);

        // 5) 자동 채점 실행 (Python 호출)
        scoreService.runScore(competition, competition.getTestFilePath(), save.getFilePath())
                .thenAccept(score -> { // 채점 완료 시 이 블록이 별도 스레드에서 실행됩니다.

                    if (score < 0) {
                        // 채점 스크립트 오류 발생 (로그는 ScoreService에서 남겼을 것)
                        // 실패 처리를 위해 score를 -1로 유지하고 DB에 저장
                        save.setScore(-1.0);
                        csvSaveRepository.save(save);
                        return;
                    }

                    // 6) 제출 CSV의 score 업데이트
                    save.setScore(score);
                    csvSaveRepository.save(save);

                    // 7) Leaderboard 점수 반영 (랭킹 로직 실행)
                    leaderboardService.updateScore(user, competition, score);

                })
                .exceptionally(ex -> { // 비동기 작업 중 예외 발생 시 처리
                    log.error("비동기 채점 프로세스 최종 오류 발생:", ex);
                    // DB에 실패 기록 (-1.0) 남기기
                    save.setScore(-1.0);
                    csvSaveRepository.save(save);
                    return null;
                });

        // 8) 클라이언트에게 즉시 응답 반환
        // HTTP 202 Accepted (요청 접수 완료) 코드를 사용하는 것이 일반적입니다.
        return ResponseEntity
                .accepted()
                .body("SUBMIT_ACCEPTED: 제출이 접수되었으며 백그라운드에서 채점 중입니다.");
    }

    @GetMapping("/csv/{saveId}/download")
    public ResponseEntity<?> downloadCSV(@PathVariable Long saveId) {
        CompetitionCSVSave save = csvSaveRepository.findById(saveId)
                .orElse(null);

        if (save == null || save.getFilePath() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FILE_NOT_FOUND");
        }

        File file = new File(save.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FILE_NOT_EXIST");
        }

        try {
            byte[] data = Files.readAllBytes(file.toPath());

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + save.getFileName() + "\"")
                    .body(data);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("DOWNLOAD_ERROR");
        }
    }

    //리더보드 다운로드 (추가)
    @GetMapping("/csv/{saveId}/download2")
    public ResponseEntity<Resource> downloadCSV2(@PathVariable Long saveId) {

        System.out.println("/api/competitions/csv/${saveId}/download2");

        CompetitionCSVSave save = csvSaveRepository.findById(saveId)
                .orElse(null);

        if (save == null || save.getFilePath() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        File file = new File(save.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Path path = file.toPath();
            UrlResource resource = new UrlResource(path.toUri());

            String encodedName = UriUtils.encode(save.getFileName(), StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedName + "\"") // ★ 파일 다운로드
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}