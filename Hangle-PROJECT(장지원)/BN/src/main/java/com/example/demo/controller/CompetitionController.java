package com.example.demo.controller;

import com.example.demo.domain.competition.dtos.*;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/competitions")
public class CompetitionController {

    private final CompetitionService service;

    // ✅ 대회 목록 조회 (페이지, 정렬, 검색 포함)
    @GetMapping
    public Page<CompetitionDto> list(
            @RequestParam(defaultValue = "OPEN") Status status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "startAt,DESC") String sort
    ) {
        String[] s = sort.split(",");
        Sort.Direction dir = (s.length > 1 && s[1].equalsIgnoreCase("ASC"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort by = Sort.by(dir, s[0]);
        return service.search(status, keyword, page, size, by);
    }

    // ✅ 단건 상세 조회
    @GetMapping("/{id}")
    public CompetitionDto detail(@PathVariable Long id) {
        return service.get(id);
    }

    // ✅ 등록
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetitionDto> create(@Valid @RequestBody CompetitionCreateRequest req) {
        CompetitionDto saved = service.create(req);
        return ResponseEntity.created(URI.create("/api/competitions/" + saved.id()))
                .body(saved);
    }

    // ✅ 수정
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetitionDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CompetitionUpdateRequest req
    ) {
        CompetitionDto updated = service.update(id, req);
        return ResponseEntity.ok(updated);
    }

    // ✅ 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
