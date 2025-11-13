package com.example.demo.domain.user.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="accessToken",columnDefinition = "TEXT",nullable = false)
    private String accessToken;
    @Column(name="refreshToken",columnDefinition = "TEXT",nullable = false)
    private String refreshToken;
    @Column(name="userid",nullable = false)
    private String userid;
    @Column(name="createdAt",columnDefinition = "DATETIME",nullable = true)
    private LocalDateTime createdAt;
}
