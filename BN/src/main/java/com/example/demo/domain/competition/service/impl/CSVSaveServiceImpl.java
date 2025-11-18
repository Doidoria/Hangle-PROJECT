package com.example.demo.domain.competition.service.impl;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.competition.repository.CompetitionCSVSaveRepository;
import com.example.demo.domain.competition.service.CSVSaveService;
import com.example.demo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CSVSaveServiceImpl implements CSVSaveService {

    private final CompetitionCSVSaveRepository csvSaveRepository;

    @Override
    public CompetitionCSVSave saveCSV(MultipartFile file, User user, Competition competition) {

        CompetitionCSVSave save = CompetitionCSVSave.builder()
                .competitionId(competition.getId())
                .userid(user.getUserid())
                .fileName(file.getOriginalFilename())
                .filePath("/uploads/" + file.getOriginalFilename()) // 실제 파일 저장은 나중에
                .submittedAt(LocalDateTime.now())
                .score(0.0)
                .build();

        return csvSaveRepository.save(save);
    }
}
