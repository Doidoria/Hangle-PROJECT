package com.example.demo.controller;

import com.example.demo.domain.competition.dtos.CompetitionCreateRequest;
import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.dtos.CompetitionUpdateRequest;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.service.CompetitionService;
import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.competition.service.CSVSaveService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.config.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService service;
    private final CSVSaveService csvSaveService;
    private final UserService userService;      // 안 쓰면 나중에 삭제해도 됨
    private final UserRepository userRepository;

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

    @PostMapping
    public ResponseEntity<CompetitionDto> create(@Valid @RequestBody CompetitionCreateRequest req) {
        CompetitionDto created = service.create(req);
        URI location = URI.create("/api/competitions/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public CompetitionDto update(@PathVariable Long id, @Valid @RequestBody CompetitionUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{competitionId}/submissions")
    public ResponseEntity<?> submit(
            @PathVariable Long competitionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userid") String userid
    ) {
        // 파일 검증
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("FILE_REQUIRED");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("CSV_ONLY");
        }

        // 유저 조회
        User user = userRepository.findByUserid(userid);
        if (user == null) {
            return ResponseEntity.badRequest().body("INVALID_USER");
        }

        // 대회 조회
        Competition competition = service.findEntity(competitionId);

        // CSV 저장
        CompetitionCSVSave save = csvSaveService.saveCSV(file, user, competition);

        return ResponseEntity.ok("SUBMIT_OK");
    }
}
