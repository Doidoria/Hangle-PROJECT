package com.example.demo.domain.mySetting.entity;

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
public class Setting {

    // User 엔티티의 PK를 그대로 가져와 Setting의 PK이자 FK로 사용
    @Id
    @Column(name = "user_id")
    private String userId;

    // 이메일 변경 기능 구현을 위한 필드
    @Column(unique = true) // 이메일은 중복되면 안 되므로 UNIQUE 제약조건 추가
    private String email;

    // 전화번호 변경/인증 기능을 위한 필드 (나중에 사용)
    private String phoneNumber;

    // User 엔티티와의 1:1 관계 설정
    @OneToOne
    @MapsId // Setting의 PK를 User의 PK(userId)에 매핑
    @JoinColumn(name = "user_id")
    private User user;
}