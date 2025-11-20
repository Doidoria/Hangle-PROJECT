package com.example.demo.domain.competition.service.impl;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.competition.repository.CompetitionCSVSaveRepository;
import com.example.demo.domain.competition.service.CSVSaveService;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
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

        // 업로드 경로 = /uploads/dataset/{competitionId}/
        String uploadDir = System.getProperty("user.dir")
                + "/uploads/dataset/"
                + competition.getId()
                + "/";

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        // 파일명: train.csv 또는 test.csv 고정
        String fileName = type.equals("train") ? "train.csv" : "test.csv";
        Path path = Paths.get(uploadDir + fileName);

        // 실제 파일 저장
        try {
            Files.copy(file.getInputStream(), path,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("CSV 저장 실패", e);
        }

        // DB 기록 저장
        CompetitionCSVSave save = CompetitionCSVSave.builder()
                .competitionId(competition.getId())
                .userid(null)   // dataset 파일에는 userid 없음
                .fileName(fileName)
                .filePath(path.toString())
                .submittedAt(LocalDateTime.now())
                .score(null)    // dataset은 score 없음
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
        String uploadDir = System.getProperty("user.dir")
                + "/uploads/submission/"
                + competition.getId()
                + "/";

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        // 원본 파일명 유지하되 UUID 붙여 충돌 방지
        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "_" + originalName;

        Path path = Paths.get(uploadDir + storedName);

        // 실제 파일 복사
        try {
            Files.copy(file.getInputStream(), path,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("제출 CSV 저장 실패", e);
        }

        // 제출 기록 DB 저장
        CompetitionCSVSave save = CompetitionCSVSave.builder()
                .competitionId(competition.getId())
                .userid(user.getUserid())    // 제출한 사람
                .fileName(originalName)      // 사용자에게 보이는 이름
                .filePath(path.toString())   // 실제 저장 경로
                .submittedAt(LocalDateTime.now())
                .score(0.0)                  // 채점 전
                .build();

        return csvSaveRepository.save(save);
    }

}
