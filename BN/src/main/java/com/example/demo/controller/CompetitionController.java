package com.example.demo.controller;

import com.example.demo.domain.competition.dtos.CompetitionCreateRequest;
import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.dtos.CompetitionUpdateRequest;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.service.CompetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

// CompetitionController.java
@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService service;

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
}
