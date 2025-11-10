package com.example.demo.domain.user.entity;

import com.example.demo.domain.myProfile.entity.Profile;
import com.example.demo.domain.mySetting.entity.Setting;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class User {
    @Id
    @Column(length = 100) // ✅ 명시적으로 길이 지정 (PK 안정성)
    private String userid;
    @Column(nullable = false, length = 50)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, length = 20)
    private String role;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime lastLoginAt;

    @Column(nullable = true)
    private String provider;
    @Column(nullable = true)
    private String providerId;

    // Profile (1:1) - 주인 관계는 Profile 쪽에서 user_id로
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    // Setting (1:1) - 주인 관계는 Setting 쪽에서 user_id로
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Setting setting;

}
