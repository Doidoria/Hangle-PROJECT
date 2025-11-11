package com.example.demo.domain.user.entity;

import com.example.demo.domain.myProfile.entity.Profile;
import com.example.demo.domain.mySetting.entity.Setting;
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
public class User {
	@Id
    private String userid;
	private String username;
	private String password;
	private String role;

    // Profile과의 1:1 관계 설정 (User가 Profile을 소유함)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Setting setting;

}
