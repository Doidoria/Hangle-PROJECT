package com.example.demo.domain.competition.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class CompetitionDetailDto extends CompetitionDto {
    private String trainDownloadUrl;
    private String testDownloadUrl;
}