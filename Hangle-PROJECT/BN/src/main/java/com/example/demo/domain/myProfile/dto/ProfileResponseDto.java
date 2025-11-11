package com.example.demo.domain.myProfile.dto;
import com.example.demo.domain.myProfile.entity.Profile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponseDto {
    private String userId;
    private String displayName;
    private String profileImageUrl;

    public static ProfileResponseDto fromEntity(Profile profile) {
        return ProfileResponseDto.builder()
                .userId(profile.getUserId())
                .displayName(profile.getDisplayName())
                .profileImageUrl(profile.getProfileImageUrl())
                .build();
    }
}
