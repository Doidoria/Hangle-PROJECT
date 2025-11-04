package com.example.demo.domain.dto;

import java.time.LocalDate;

import com.example.demo.domain.entity.User;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
	private String username;
	private String password;
	private String role;

	//OAUTH2 CLIENT INFO
	private String provider;
	private String providerId;
	//DTO->ENTITY
	public User toEntity(){
		return User.builder()
				.username(this.username)
				.password(this.password)
				.role("ROLE_USER")
				.build();
	}
	//ENTITY->DTO
	public static UserDto toDto(User user){
		return UserDto.builder()
					.username(user.getUsername())
					.password(user.getPassword())
					.role(user.getRole())
					.build();
	}
}
