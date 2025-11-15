package com.example.demo.controller;

import com.example.demo.domain.competition.dtos.CompetitionCreateRequest;
import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.dtos.CompetitionUpdateRequest;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.service.CompetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import com.example.demo.domain.competition.repository.CSVSave;



import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

// CompetitionController.java
@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {


    private final CompetitionService service;

    private final CSVSave csvSaveRepository;

    @GetMapping
    public Page<CompetitionDto> getAll(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return service.search(status, keyword, page, size, sort);
    }

    @GetMapping("/{id}")
    public CompetitionDto getById(@PathVariable Long id) {
        return service.get(id);
    }

    // 생성
    @PostMapping
    public ResponseEntity<CompetitionDto> create(@Valid @RequestBody CompetitionCreateRequest req) {
        CompetitionDto created = service.create(req);
        URI location = URI.create("/api/competitions/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    // 수정
    @PutMapping("/{id}")
    public CompetitionDto update(@PathVariable Long id, @Valid @RequestBody CompetitionUpdateRequest req) {
        return service.update(id, req);
    }

    // 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // CSV 제출
    @PostMapping("/{competitionId}/submit")  // ★ 수정됨
    public ResponseEntity<?> submitResult(
            @PathVariable Long competitionId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "파일이 비어있습니다."));
            }

            String username = authentication != null ? authentication.getName() : "anonymous";

            String uploadDir = "uploads/submissions/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            file.transferTo(filePath.toFile());

            CompetitionCSVSave saveEntity = CompetitionCSVSave.builder()
                    .competitionId(competitionId)
                    .userid(username)
                    .fileName(fileName)
                    .filePath(filePath.toString())
                    .submittedAt(LocalDateTime.now())
                    .build();

            csvSaveRepository.save(saveEntity);

            return ResponseEntity.ok(Map.of(
                    "message", "CSV 업로드 성공",
                    "fileName", fileName
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "업로드 실패: " + e.getMessage()));
        }
    }

    // CSV 제출 전 검증
    @PostMapping("/{competitionId}/validate")
    public ResponseEntity<?> validateSubmission(
            @PathVariable Long competitionId,
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일이 비어있습니다."));
        }

        // 파일 이름 검증 (CSV만 허용)
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "CSV 파일만 제출 가능합니다."));
        }

        return ResponseEntity.ok(Map.of(
                "message", "검증 통과",
                "competitionId", competitionId
        ));
    }


}

