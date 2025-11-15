package com.example.demo.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
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
    @Column(length = 15)
    private String phone;
    @Column(nullable = false)
    @Builder.Default
    private Boolean isCertified = false; // 본인 인증 완료 상태 저장 필드

    @Column(name = "image_url")
    private String profileImageUrl;
    @Column(nullable = true)
    private String theme;  // "light" 또는 "dark"
    @Column(length = 500)
    private String introduction;  // 자기소개 (null 가능)

    @Column(nullable = true)
    private String provider;
    @Column(nullable = true)
    private String providerId;

}
