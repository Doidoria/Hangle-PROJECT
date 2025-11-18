package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.entity.Competition;
import com.example.demo.domain.competition.entity.CompetitionCSVSave;
import com.example.demo.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface CSVSaveService {
    CompetitionCSVSave saveCSV(MultipartFile file, User user, Competition competition);
}
