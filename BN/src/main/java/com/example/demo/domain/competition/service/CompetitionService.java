package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.dtos.CompetitionCreateRequest;
import com.example.demo.domain.competition.dtos.CompetitionDto;
import com.example.demo.domain.competition.dtos.CompetitionMapper;
import com.example.demo.domain.competition.dtos.CompetitionUpdateRequest;
import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository repository;

    // ✅ CSV 저장 전담 서비스 주입 (dataset + 제출 CSV)
    private final CSVSaveService csvSaveService;

    // 파일 저장 루트(로컬)
    private static final String ROOT_PATH = "uploads/competitions/";

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
        c.setPurpose(req.description());
        c.setDetail(req.detail());
        c.setStatus(Status.valueOf(req.status()));
        c.setStartAt(req.startAt());
        c.setEndAt(req.endAt());
        if (req.evaluationMetric() != null) c.setEvaluationMetric(req.evaluationMetric());
        if (req.prizeTotal() != null)       c.setPrizeTotal(req.prizeTotal());
        Competition saved = repository.save(c);
        return CompetitionMapper.toDto(saved);
    }

    // 파일 업로드
    @Transactional
    public CompetitionDto createWithFiles(
            CompetitionCreateRequest req,
            MultipartFile trainFile,
            MultipartFile testFile,
            MultipartFile customScoreFile
    ) {
        try {
            // 1) 기본 엔티티 저장 (ID 먼저 생성)
            Competition c = new Competition();
            c.setTitle(req.title());
            c.setPurpose(req.description());
            c.setDetail(req.detail());
            c.setStatus(Status.valueOf(req.status()));
            c.setStartAt(req.startAt());
            c.setEndAt(req.endAt());
            if (req.evaluationMetric() != null) c.setEvaluationMetric(req.evaluationMetric());
            if (req.prizeTotal() != null)       c.setPrizeTotal(req.prizeTotal());

            Competition saved = repository.save(c);

            // 2) 파일 저장 경로 생성
//            String compDir = ROOT_PATH + saved.getId() + "/";
//            File dir = new File(compDir);
//            if (!dir.exists()) dir.mkdirs();
            // 2) CSV 저장
            CompetitionCSVSave trainSave = csvSaveService.saveDatasetFile(trainFile, saved, "train");
            CompetitionCSVSave testSave  = csvSaveService.saveDatasetFile(testFile, saved, "test");

            // 2-1) 커스텀 score.py 업로드 처리
            if (customScoreFile != null && !customScoreFile.isEmpty()) {
                String scorePath = "uploads/competitions/" + saved.getId() + "/score.py";

                File scoreFile = new File(scorePath);
                scoreFile.getParentFile().mkdirs(); // 디렉토리 생성
                customScoreFile.transferTo(scoreFile);

                saved.setCustomScorePath(scorePath);
            }

            // 3) 파일 저장
            // train.csv
//            String trainPath = compDir + "train.csv";
//            Files.copy(trainFile.getInputStream(), Path.of(trainPath),
//                    StandardCopyOption.REPLACE_EXISTING);
//
//            // test.csv
//            String testPath = compDir + "test.csv";
//            Files.copy(testFile.getInputStream(), Path.of(testPath),
//                    StandardCopyOption.REPLACE_EXISTING);

            // 4) 엔티티에 파일 경로 기록
//            saved.setTrainFilePath("/" + trainPath);
//            saved.setTestFilePath("/" + testPath);
            // 3) Competition에 ID + 파일 경로 저장
            saved.setTrainDatasetSaveId(trainSave.getId());
            saved.setTestDatasetSaveId(testSave.getId());

            saved.setTrainFilePath(trainSave.getFilePath());
            saved.setTestFilePath(testSave.getFilePath());

            repository.save(saved);

            // 5) DTO 변환 후 반환
            return CompetitionMapper.toDto(saved);

        } catch (Exception e) {
            throw new RuntimeException("대회 생성 중 파일 저장 오류: " + e.getMessage(), e);
        }
    }

    // 수정
    @Transactional
    public CompetitionDto update(Long id, CompetitionUpdateRequest req) {
        Competition c = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found: " + id));

        c.setTitle(req.title());
        c.setPurpose(req.description());
        c.setDetail(req.detail());                               // ✅ 상세 설명
        if (req.status() != null) c.setStatus(Status.valueOf(req.status()));
        c.setStartAt(req.startAt());
        c.setEndAt(req.endAt());
        if (req.evaluationMetric() != null) c.setEvaluationMetric(req.evaluationMetric()); // ✅
        if (req.prizeTotal() != null)       c.setPrizeTotal(req.prizeTotal());             // ✅

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

    // ==========================================
    // CSV 제출용 Competition 엔티티 조회
    // ==========================================
    @Transactional(readOnly = true)
    public Competition findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found: " + id));
    }




}
