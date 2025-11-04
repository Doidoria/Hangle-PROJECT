package com.example.demo.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinDto {
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
}
