package com.example.demo.controller;

import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

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
        // 최신 등록 순으로 정렬
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return service.search(status, keyword, page, size, sort);
    }

    // 개별 대회 상세조회
    @GetMapping("/{id}")
    public CompetitionDto getById(@PathVariable Long id) {
        return service.get(id);
    }
}
