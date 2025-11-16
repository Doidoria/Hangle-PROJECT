package com.example.demo.controller;

import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.user.dto.UserDto;
import com.example.demo.domain.user.repository.JwtTokenRepository;
import com.example.demo.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.domain.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@Slf4j
@RequiredArgsConstructor
public class UserRestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    @PostMapping(value = "/join", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> join_post(@Valid @RequestBody UserDto userDto, BindingResult result) {
        log.info("POST /join..." + userDto);

        if (result.hasErrors()) {
            String errorMessage = result.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest().body(Map.of("error", errorMessage));
        }

        User existingUser = userRepository.findByUserid(userDto.getUserid());
        if (existingUser != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미 존재하는 사용자입니다."));
        }

        User user = userDto.toEntity();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }

    @Operation(summary = "로그인")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> login(@RequestBody UserDto userDto, HttpServletResponse resp) throws IOException {
        log.info("POST /login..." + userDto);
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByUserid(userDto.getUserid());
        if (userDto.getUserid() == null || userDto.getUserid().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "아이디(이메일)를 입력해주세요."));
        }
        if (!userDto.getUserid().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "아이디(이메일) 형식으로 입력해주세요."));
        }
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호를 입력해주세요."));
        }
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "존재하지 않는 사용자입니다."));
        }
        if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "비밀번호가 일치하지 않습니다."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getUserid(), userDto.getPassword())
            );

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

            redisUtil.save("RT:" + authentication.getName(), tokenInfo.getRefreshToken());

            response.put("state", "success");
            response.put("message", "로그인에 성공했습니다.");
            response.put("username", user.getUsername());
            response.put("userid", user.getUserid());
            response.put("token", tokenInfo.getAccessToken());

            Cookie accessCookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, tokenInfo.getAccessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME / 1000);

            Cookie userCookie = new Cookie("userid", authentication.getName());
            userCookie.setHttpOnly(true);
            userCookie.setSecure(false);
            userCookie.setPath("/");
            userCookie.setMaxAge(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000);

            resp.addCookie(accessCookie);
            resp.addCookie(userCookie);

        } catch (AuthenticationException e) {
            response.put("state", "fail");
            response.put("message", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @Operation(summary = "AccessToken 검증", description = "현재 Access Token이 유효한지 확인합니다.")
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println(" [VALIDATE] auth = " + auth);

        // 1) 인증 객체 없음
        if (auth == null) {
            System.out.println(" [VALIDATE] 인증 객체 없음 → 401");
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }

        // 2) principal 이 anonymousUser 이면 로그인 X
        Object principal = auth.getPrincipal();
        if (principal == null || principal.equals("anonymousUser")) {
            System.out.println(" [VALIDATE] anonymousUser → 401");
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }

        // 3) 권한(roles)이 없다면 로그인 X
        if (auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            System.out.println(" [VALIDATE] 권한 없음 → 401");
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }

        // 4) 정상 로그인
        System.out.println(" [VALIDATE] 인증된 사용자 → 200 OK");
        return new ResponseEntity<>("", HttpStatus.OK);
    }


    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }
}
