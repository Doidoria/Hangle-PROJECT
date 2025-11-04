package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CompetitionService {
    List<CompetitionDto> listCompetitions();
    CompetitionDetailDto getCompetition(Long id);
    byte[] downloadDataset(Long id, String type); // "train" | "test"
    SubmissionResponseDto submit(Long id, String userid, MultipartFile file);
    List<SubmissionDto> mySubmissions(Long id, String userid);
}
