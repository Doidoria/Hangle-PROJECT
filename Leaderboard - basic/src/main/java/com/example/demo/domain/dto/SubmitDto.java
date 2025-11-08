package com.example.demo.domain.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubmitDto {

    private Long submitId;
    private Long compId;
    private String compName;
    private Double best_score;

}
