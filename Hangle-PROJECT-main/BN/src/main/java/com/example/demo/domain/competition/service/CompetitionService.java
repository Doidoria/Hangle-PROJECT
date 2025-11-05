package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.dtos.CompetitionCreateRequest;
import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.dtos.CompetitionMapper;
import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.repository.CompetitionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository repository;

    public Page<CompetitionDto> search(Status status, String keyword, int page, int size, Sort sort) {
        Specification<Competition> spec = (root, q, cb) -> {
            Predicate p = cb.conjunction();
            if (status != null) {
                p = cb.and(p, cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim() + "%";
                p = cb.and(p, cb.like(root.get("title"), like));
            }
            return p;
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAll(spec, pageable).map(CompetitionMapper::toDto);
    }

    public CompetitionDto get(Long id) {
        Competition c = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found: " + id));
        return CompetitionMapper.toDto(c);
    }

    // ✅ 등록
    @Transactional
    public CompetitionDto create(CompetitionCreateRequest req) {
        Competition c = new Competition();
        c.setTitle(req.title());
        c.setDescription(req.description());
        c.setStatus(req.status());
        c.setStartAt(req.startAt());
        c.setEndAt(req.endAt());
        // createdAt은 엔티티에서 기본값(LocalDateTime.now())로 셋됨
        Competition saved = repository.save(c);
        return CompetitionMapper.toDto(saved);
    }
}
