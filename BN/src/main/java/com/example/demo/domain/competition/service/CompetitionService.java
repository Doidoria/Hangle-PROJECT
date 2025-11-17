package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.dtos.CompetitionCreateRequest;
import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.dtos.CompetitionMapper;
import com.example.demo.domain.competition.dtos.CompetitionUpdateRequest;
import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.Status;
import com.example.demo.domain.competition.repository.CompetitionRepository;
import com.example.demo.global.exception.ConflictException;
import com.example.demo.global.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository repository;

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

    @Transactional(readOnly = true)
    public CompetitionDto get(Long id) {
        Competition c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found: " + id));
        return CompetitionMapper.toDto(c);
    }

    // 생성
    @Transactional
    public CompetitionDto create(CompetitionCreateRequest req) {
        Competition c = new Competition();
        c.setTitle(req.title());
        c.setDescription(req.description());
        c.setStatus(req.status());              // req.status() 타입이 Status
        c.setStartAt(req.startAt());
        c.setEndAt(req.endAt());
        Competition saved = repository.save(c);
        return CompetitionMapper.toDto(saved);
    }

    // 수정
    @Transactional
    public CompetitionDto update(Long id, CompetitionUpdateRequest req) {
        Competition c = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found: " + id));

        c.setTitle(req.title());
        c.setDescription(req.description());
        c.setStatus(Status.valueOf(req.status()));  // create, update 둘 다
        c.setStartAt(req.startAt());
        c.setEndAt(req.endAt());

        // JPA 영속 상태라 save 호출 없이도 flush 시점에 반영되지만, 명시적으로 save 해도 OK
        Competition updated = repository.save(c);
        return CompetitionMapper.toDto(updated);
    }

    // 삭제
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
