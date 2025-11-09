package com.example.demo.domain.competition.dtos;

import com.example.demo.domain.competition.entity.Competition;

public final class CompetitionMapper {
    private CompetitionMapper() {}

    public static CompetitionDto toDto(Competition c) {
        return new CompetitionDto(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getStatus(),
                c.getStartAt(),
                c.getEndAt()
        );
    }
}
