package com.example.demo.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsernameUpdateRequestDto {

    @NotBlank(message = "새로운 사용자 이름은 필수입니다.")
    @Size(min = 4, max = 20, message = "사용자 이름은 4자에서 20자 사이여야 합니다.")
    private String newUsername;
}