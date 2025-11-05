package com.example.demo.controller;

import com.example.demo.domain.competition.dtos.CompetitionCreateRequest;
import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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

    @GetMapping
    public Page<CompetitionDto> list(
            @RequestParam(defaultValue = "OPEN") Status status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt,DESC") String sort
    ) {
        String[] s = sort.split(",");
        Sort.Direction dir = (s.length > 1 && s[1].equalsIgnoreCase("ASC")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort by = Sort.by(dir, s[0]);
        return service.search(status, keyword, page, size, by);
    }

    @GetMapping("/{id}")
    public CompetitionDto detail(@PathVariable Long id) {
        return service.get(id);
    }

    // ✅ 등록(POST)
    @PostMapping
    public ResponseEntity<CompetitionDto> create(@Valid @RequestBody CompetitionCreateRequest req) {
        CompetitionDto saved = service.create(req);
        // 201 Created + Location 헤더
        return ResponseEntity.created(URI.create("/api/competitions/" + saved.id()))
                .body(saved);
    }
}
