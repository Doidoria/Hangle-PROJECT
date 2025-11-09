package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.dtos.*;
import com.example.demo.domain.competition.entity.*;
import com.example.demo.domain.competition.repository.CompetitionRepository;
import com.example.demo.exception.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository repository;

    // ✅ 검색 (상태 + 키워드 + 페이징)
    @Transactional(readOnly = true)
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

    // ✅ 단일 조회
    @Transactional(readOnly = true)
    public CompetitionDto get(Long id) {
        Competition c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found: " + id));
        return CompetitionMapper.toDto(c);
    }

    // ✅ 생성
    @Transactional
    public CompetitionDto create(CompetitionCreateRequest req) {
        Competition c = new Competition();
        c.setTitle(req.title());
        c.setSummary(req.summary());
        c.setDescription(req.description());
        c.setPrize(req.prize());
        c.setDatasetUrl(req.datasetUrl());
        c.setRuleUrl(req.ruleUrl());
        c.setStatus(req.status());   // Status Enum 타입 그대로
        c.setStartAt(req.startAt());
        c.setEndAt(req.endAt());

        Competition saved = repository.save(c);
        return CompetitionMapper.toDto(saved);
    }

    // ✅ 수정
    @Transactional
    public CompetitionDto update(Long id, CompetitionUpdateRequest req) {
        Competition c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found: " + id));

        c.setTitle(req.title());
        c.setSummary(req.summary());
        c.setDescription(req.description());
        c.setPrize(req.prize());
        c.setDatasetUrl(req.datasetUrl());
        c.setRuleUrl(req.ruleUrl());
        c.setStatus(req.status());     // Enum이면 그대로, String이면 valueOf()로 변경
        c.setStartAt(req.startAt());
        c.setEndAt(req.endAt());

        Competition updated = repository.save(c);
        return CompetitionMapper.toDto(updated);
    }

    // ✅ 삭제
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Competition not found: " + id);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("참조 중인 데이터가 있어 삭제할 수 없습니다: " + id);
        }
    }
}
