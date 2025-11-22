package com.example.demo.domain.competition.service.impl;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.competition.repository.CompetitionCSVSaveRepository;
import com.example.demo.domain.competition.repository.CompetitionRepository;
import com.example.demo.domain.competition.service.CSVSaveService;
import com.example.demo.domain.competition.service.ScoreService;
import com.example.demo.domain.leaderboard.service.LeaderboardService;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CSVSaveServiceImpl implements CSVSaveService {

    private final CompetitionCSVSaveRepository csvSaveRepository;
    private final ScoreService scoreService;
    private final LeaderboardService leaderboardService;
    private final CompetitionRepository competitionRepository;

    @Value("${file.upload-dir}") // 값 주입
    private String uploadDir;

    /* ============================================================
     *  🔥 [A] Dataset 저장 (train.csv / test.csv)
     *      - 대회 생성 시 호출됨
     *      - userid 없음
     *      - score 없음
     * ============================================================ */
    @Override
    public CompetitionCSVSave saveDatasetFile(MultipartFile file,
                                              Competition competition,
                                              String type) {

        // 경로: (설정된경로)/dataset/{competitionId}/
        Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetDir = rootPath.resolve("dataset")
                .resolve(String.valueOf(competition.getId()));

        File dir = targetDir.toFile();
        if (!dir.exists()) dir.mkdirs();

        // 파일명: train.csv 또는 test.csv 고정
        String fileName = type.equals("train") ? "train.csv" : "test.csv";
        Path filePath = targetDir.resolve(fileName);

        // 실제 파일 저장
        try {
            Files.copy(file.getInputStream(), filePath,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("CSV 저장 실패", e);
        }

        // DB 기록 저장
        CompetitionCSVSave save = CompetitionCSVSave.builder()
                .competitionId(competition.getId())
                .userid(null)
                .fileName(fileName)
                .filePath(filePath.toString()) // Path 객체의 문자열 사용
                .submittedAt(LocalDateTime.now())
                .score(null)
                .build();

        return csvSaveRepository.save(save);
    }


    /* ============================================================
     *  🔥 [B] 참가자 제출 CSV 저장
     *      - 참가자가 제출할 때 호출됨
     *      - userid 기록 필요
     *      - score 기본값 0.0 (AI 채점 후 업데이트)
     * ============================================================ */
    @Override
    public CompetitionCSVSave saveCSV(MultipartFile file,
                                      User user,
                                      Competition competition) {

        // 업로드 경로 = /uploads/submission/{competitionId}/
        Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetDir = rootPath.resolve("submission")
                .resolve(String.valueOf(competition.getId()));

        File dir = targetDir.toFile();
        if (!dir.exists()) dir.mkdirs();

        // 원본 파일명 유지하되 UUID 붙여 충돌 방지
        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "_" + originalName;

        Path filePath = targetDir.resolve(storedName);

        // 실제 파일 복사
        try {
            Files.copy(file.getInputStream(), filePath,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("제출 CSV 저장 실패", e);
        }

        // 제출 기록 DB 저장
        CompetitionCSVSave save = CompetitionCSVSave.builder()
                .competitionId(competition.getId())
                .userid(user.getUserid())
                .fileName(originalName)
                .filePath(filePath.toString()) // Path 객체의 문자열 사용
                .submittedAt(LocalDateTime.now())
                .score(0.0)
                .build();

        // 첫 제출인지 확인
        boolean isFirstSubmission =
                !csvSaveRepository.existsByCompetitionIdAndUserid(competition.getId(), user.getUserid());

        save = csvSaveRepository.save(save);

        // 2) 점수 계산 (핵심 부분)
        String answerPath = competition.getTestFilePath();
        if (answerPath == null || answerPath.isBlank()) {
            throw new RuntimeException("정답 파일(test.csv) 경로가 존재하지 않습니다.");
        }
        String submitPath = save.getFilePath();

        // ScoreService 실행 → Python 채점
        double score = scoreService.runScore(competition, answerPath, submitPath);

        // 계산된 점수 저장
        save.setScore(score);
        csvSaveRepository.save(save);

        // 참가자 수 +1
        if (isFirstSubmission) {
            competition.setParticipantCount(competition.getParticipantCount() + 1);
            competitionRepository.save(competition);
        }

        // 리더보드 반영
        leaderboardService.leaderBoardAdd(user, competition, save);

        return save;
    }

}
