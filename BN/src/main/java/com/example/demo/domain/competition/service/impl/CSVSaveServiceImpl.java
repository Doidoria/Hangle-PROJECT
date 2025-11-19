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
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CSVSaveServiceImpl implements CSVSaveService {

    private final CompetitionCSVSaveRepository csvSaveRepository;

    @Override
    public CompetitionCSVSave saveCSV(MultipartFile file, User user, Competition competition) {

        /* ==================================================
         *  🔥 1) 업로드 경로 생성
         * ================================================== */
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        /* ==================================================
         *  🔥 2) 파일 저장 (UUID 로 이름 변경)
         * ================================================== */
        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "_" + originalName;
        Path path = Paths.get(uploadDir + storedName);

        try {
            Files.copy(file.getInputStream(), path);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        /* ==================================================
         *  🔥 3) DB 기록 저장
         * ================================================== */
        CompetitionCSVSave save = CompetitionCSVSave.builder()
                .competitionId(competition.getId())
                .userid(user.getUserid())
                .fileName(originalName)          // 사용자가 업로드한 실제 파일명
                .filePath(path.toString())       // 서버에 저장된 실제 경로
                .submittedAt(LocalDateTime.now())
                .score(0.0)
                .build();

        return csvSaveRepository.save(save);
    }
}
