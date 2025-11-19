package com.example.demo.controller;

import com.example.demo.domain.competition.dtos.CompetitionCreateRequest;
import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.dtos.CompetitionUpdateRequest;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.competition.repository.CompetitionCSVSaveRepository;
import com.example.demo.domain.competition.service.CompetitionService;
import com.example.demo.domain.competition.service.CSVSaveService;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;


@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;
    private final CompetitionCSVSaveRepository csvSaveRepository;
    private final CSVSaveService csvSaveService;
    private final UserService userService;
    private final LeaderboardService leaderboardService;

    /** ===========================
     * 대회 전체 조회
     * =========================== */
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

    /** ===========================
     * 단건 조회
     * =========================== */
    @GetMapping("/{id}")
    public CompetitionDto getById(@PathVariable Long id) {
        return competitionService.get(id);
    }

    /** ===========================
     * 생성
     * =========================== */
    @PostMapping
    public ResponseEntity<CompetitionDto> create(@Valid @RequestBody CompetitionCreateRequest req) {
        CompetitionDto created = competitionService.create(req);
        URI location = URI.create("/api/competitions/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    /** ===========================
     * 수정
     * =========================== */
    @PutMapping("/{id}")
    public CompetitionDto update(@PathVariable Long id, @Valid @RequestBody CompetitionUpdateRequest req) {
        return competitionService.update(id, req);
    }

    /** ===========================
     * 삭제
     * =========================== */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        competitionService.delete(id);
    }


    /** ======================================================
     *  🔥🔥 CSV 제출 API — 병합본 기준 완성버전
     * ====================================================== */
    @PostMapping("/{competitionId}/submit")
    public ResponseEntity<?> submit(
            @PathVariable Long competitionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userid") String userid
    ) {
        // 1) 유저 조회
        User user = userService.findByUserid(userid);
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

        return ResponseEntity.ok("SUBMIT_OK");
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
}
