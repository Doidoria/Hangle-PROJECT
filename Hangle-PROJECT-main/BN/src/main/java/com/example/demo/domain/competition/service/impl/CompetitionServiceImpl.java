package com.example.demo.domain.competition.service.impl;

import com.example.demo.domain.competition.dto.*;
import com.example.demo.domain.competition.entity.Submission;
import com.example.demo.domain.competition.repository.SubmissionRepository;
import com.example.demo.domain.competition.service.CompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final SubmissionRepository submissionRepository;
    // private final CompetitionRepository competitionRepository;
    // private final ScoringService scoringService;  // 점수 계산 로직(추후 구현)
    // private final StorageService storageService;  // 파일 저장/다운로드(추후 구현)

    @Override
    public List<CompetitionDto> listCompetitions() {
        // TODO: 실제 조회
        return Collections.emptyList();
    }

    @Override
    public CompetitionDetailDto getCompetition(Long id) {
        // TODO: 실제 조회 + 다운로드 URL 생성
        return CompetitionDetailDto.builder()
                .id(id)
                .title("Demo Competition")
                .description("설명")
                .rules("규칙")
                .metric("accuracy")
                .trainDownloadUrl("/api/v1/competition/"+id+"/dataset?type=train")
                .testDownloadUrl("/api/v1/competition/"+id+"/dataset?type=test")
                .build();
    }

    @Override
    public byte[] downloadDataset(Long id, String type) {
        // TODO: 실제 파일 로딩
        return "col1,col2\n1,2\n".getBytes();
    }

    @Override
    public SubmissionResponseDto submit(Long id, String userid, MultipartFile file) {
        // TODO: 1) 파일 저장 2) 정답 로딩 3) 점수 계산 4) DB 저장 5) 랭크 계산
        double dummyScore = Math.random(); // 예시
        int attempts = submissionRepository.countByCompetitionIdAndUserid(id, userid) + 1;

        Submission saved = submissionRepository.save(
                Submission.builder()
                        .competitionId(id)
                        .userid(userid)
                        .score(dummyScore)
                        .attempt(attempts)
                        .filePath("uploads/placeholder.csv")
                        .submittedAt(LocalDateTime.now())
                        .build()
        );

        int rank = 1; // TODO: 리더보드 기반 실제 랭크 계산
        return SubmissionResponseDto.builder()
                .score(saved.getScore())
                .rank(rank)
                .build();
    }

    @Override
    public List<SubmissionDto> mySubmissions(Long id, String userid) {
        return submissionRepository.findByCompetitionIdAndUseridOrderBySubmittedAtDesc(id, userid)
                .stream().map(s -> SubmissionDto.builder()
                        .id(s.getId())
                        .competitionId(s.getCompetitionId())
                        .userid(s.getUserid())
                        .score(s.getScore())
                        .attempt(s.getAttempt())
                        .submittedAt(s.getSubmittedAt())
                        .build()
                ).toList();
    }
}
