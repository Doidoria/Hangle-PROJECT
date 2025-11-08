package com.example.demo.domain.competition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
public class CompetitionDto {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "Titanic Survival Prediction")
    private String title;
    @Schema(example = "타이타닉 생존 예측 대회")
    private String description;
    @Schema(example = "제출 형식: id,prediction ...")
    private String rules;
    @Schema(example = "accuracy")
    private String metric;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
