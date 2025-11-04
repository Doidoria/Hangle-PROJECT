package com.example.demo.domain.user.dto;

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
	private String userid;
	private String password;
	private String role;

	//OAUTH2 CLIENT INFO
	private String provider;
	private String providerId;

	//DTO->ENTITY
	public User toEntity(){
		return User.builder()
				.userid(this.userid)
				.password(this.password)
				.role("ROLE_USER")
				.build();
	}
	//ENTITY->DTO
	public static UserDto toDto(User user){
		return UserDto.builder()
					.userid(user.getUserid())
					.password(user.getPassword())
					.role(user.getRole())
					.build();
	}
}
