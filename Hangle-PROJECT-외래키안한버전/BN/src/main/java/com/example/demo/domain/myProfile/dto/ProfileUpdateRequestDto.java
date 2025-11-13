package com.example.demo.domain.myProfile.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequestDto {

    @Size(max = 50, message = "표시 이름은 50자 이내여야 합니다.")
    private String displayName;

    private String profileImageUrl;
}
