package com.example.demo.domain.myProfile.entity;

import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Profile {

    // User 엔티티의 PK를 그대로 가져와 Profile의 PK이자 FK로 사용
    @Id
    @Column(name = "user_id")
    private String userId;

    // 프로필 이미지 URL (S3, CDN 등 외부 스토리지 경로)
    private String profileImageUrl;

    // 사용자에게 보여줄 이름 (선택적)
    // User 엔티티의 username과 분리하여, 사용자가 변경 가능한 이름으로 사용 가능
    private String displayName;

    // User 엔티티와의 1:1 관계 설정
    @OneToOne
    @MapsId // Profile의 PK를 User의 PK(userId)에 매핑
    @JoinColumn(name = "user_id")
    private User user;

    // 이외 필요한 프로필 필드를 추가하세요 (예: 자기소개, 웹사이트 등)
}