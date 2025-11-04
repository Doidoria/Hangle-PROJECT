package com.example.demo.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.demo.domain.user.entity.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    @NotBlank(message = "이름을 입력하세요.")
    private String username;
    @NotBlank(message = "ID(E-mail)를 입력하세요.")
    @Email(message = "user@naver.com 형식으로 입력하세요.")
    private String userid;
    @NotBlank(message = "패스워드를 입력하세요.")
    private String password;
    @NotBlank(message = "패스워드를 재입력하세요.")
    private String repassword;
    private String role;

	//OAUTH2 CLIENT INFO
	private String provider;
	private String providerId;

	//DTO->ENTITY
    // UserDto에서 password를 암호화하여 사용 하지 않는 이유 : 데이터를 저장하는 용도기 때문 (나중에 암호화 방식 변경 시 DTO까지 수정해야 함)
	public User toEntity(){
		return User.builder()
                .username(this.username)
				.userid(this.userid)
				.password(this.password)
				.role("ROLE_USER")
				.build();
	}
	//ENTITY->DTO
	public static UserDto toDto(User user){
		return UserDto.builder()
                .username(user.getUsername())
                .userid(user.getUserid())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
	}
}
