package com.example.demo.domain.user.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.demo.domain.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    @NotBlank(message = "이름을 입력하세요.")
    private String username;
    @NotBlank(message = "ID(E-mail)를 입력하세요.")
    @Email(message = "hangle@naver.com 형식으로 입력하세요.")
    private String userid;
    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;
    @NotBlank(message = "비밀번호를 재입력하세요.")
    private String repassword;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private String introduction;
    private String profileImageUrl;
    private String theme;
    private String phone;
    private Boolean isCertified;

	//OAUTH2 CLIENT INFO
	private String provider;
	private String providerId;

	//DTO->ENTITY
    // UserDto에서 password를 암호화하여 사용 하지 않는 이유 : 데이터를 저장하는 용도기 때문 (나중에 암호화 방식 변경 시 DTO까지 수정해야 함)
	public User toEntity(){
		return User.builder()
                .id(this.id)
                .username(this.username)
                .userid(this.userid)
                .password(this.password)
                .role(this.role != null ? this.role : "ROLE_USER")
                .introduction(this.introduction)
                .profileImageUrl(this.profileImageUrl)
                .provider(this.provider)
                .providerId(this.providerId)
                .phone(this.phone)
                .isCertified(this.isCertified != null ? this.isCertified : false)
                .build();
	}
	//ENTITY->DTO
	public static UserDto toDto(User user){
		return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .userid(user.getUserid())
                .password(user.getPassword())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .introduction(user.getIntroduction())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .phone(user.getPhone())
                .isCertified(user.getIsCertified())
                .build();
	}
}
