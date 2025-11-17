package com.example.demo.controller;

import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.user.dto.UserDto;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.domain.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.nio.file.Files;
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
    //Header 방식 (Authorization: Bearer <token>)
    // - XXS 공격에 매우취약 - LocalStorage / SessionStorage에 저장시 문제 발생
    // - 쿠키방식이 비교적 안전
    @Operation(
            summary = "로그인",
            description = """
        사용자 아이디(이메일)과 비밀번호를 입력해 JWT Access Token을 발급받습니다.<br><br>
        발급된 토큰은 Swagger UI 상단의 <b>Authorize</b> 버튼을 눌러 입력하면,<br>
        인증이 필요한 API(`/user`, `/validate`, `/api/users/me`) 호출 시 자동으로 적용됩니다.<br><br>
        예시 입력:<br><code>Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...</code>""")
    @PostMapping(value = "/login" , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> login(@RequestBody UserDto userDto, HttpServletResponse resp) throws IOException {
        log.info("POST /login..." + userDto);
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByUserid(userDto.getUserid());
        boolean skipEmailCheck = (user != null && "ROLE_ADMIN".equals(user.getRole()));
        if (userDto.getUserid() == null || userDto.getUserid().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "아이디(이메일)를 입력해주세요."));
        }
        if (!skipEmailCheck && !userDto.getUserid().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
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

        try{
            System.out.println(">>> login controller in progress: " + user.getUserid());
            //사용자 인증 시도(ID/PW 일치여부 확인)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getUserid(),userDto.getPassword())
            );
            System.out.println("인증성공 : " + authentication);

            // 최근 접속 시간 갱신
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);  // DB에 반영

            //Token 생성
            TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
            System.out.println("JWT TOKEN : " + tokenInfo);

            //REDIS 에 REFRESH 저장
            redisUtil.save("RT:"+authentication.getName() , tokenInfo.getRefreshToken());
            response.put("state","success");
            response.put("message","로그인에 성공했습니다.");
            response.put("username", user.getUsername());
            response.put("userid", user.getUserid());
            response.put("token", tokenInfo.getAccessToken());

            String accessToken = tokenInfo.getAccessToken();
            String userid = authentication.getName();
            // Access Token Cookie
            resp.addHeader(
                    "Set-Cookie",
                    JwtProperties.ACCESS_TOKEN_COOKIE_NAME + "=" + accessToken
                            + "; Path=/"
                            + "; HttpOnly"
                            + "; Secure"
                            + "; SameSite=None"
                            + "; Max-Age=" + (JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME / 1000)
            );

            // User ID Cookie
            resp.addHeader(
                    "Set-Cookie",
                    "userid=" + userid
                            + "; Path=/"
                            + "; HttpOnly"
                            + "; Secure"
                            + "; SameSite=None"
                            + "; Max-Age=" + (JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000)
            );

        }catch(AuthenticationException e){
            System.out.println("인증실패 : " + e.getMessage());
            response.put("state","fail");
            response.put("message","아이디 또는 비밀번호가 올바르지 않습니다.");
            return new ResponseEntity(response,HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @PutMapping("/api/user/introduction")
    public ResponseEntity<?> updateIntroduction(@RequestBody Map<String, String> req, Authentication authentication) {
        String userid = authentication.getName();
        User user = userRepository.findByUserid(userid);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }
        String newIntro = req.get("introduction");
        user.setIntroduction(newIntro);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "message", "자기소개가 성공적으로 수정되었습니다.",
                "introduction", newIntro
        ));
    }

    @PutMapping("/api/user/update-info")
    public ResponseEntity<?> updateUserInfo(@RequestBody Map<String, String> req, Authentication authentication, HttpServletResponse resp) {
        System.out.println("현재 인증된 ID = " + authentication.getName());
        String currentUserid = authentication.getName();
        String userid = authentication.getName();
        User user = userRepository.findByUserid(userid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }
        String newUsername = req.get("username");
        String newUserid = req.get("userid");
        if (newUsername != null && !newUsername.isBlank()) {
            user.setUsername(newUsername);
        }
        if (newUserid != null && !newUserid.isBlank() && !newUserid.equals(userid)) {

            if (user.getProvider() != null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "소셜 로그인 사용자는 이메일을 변경할 수 없습니다."));
            }
            // 아이디 중복 확인
            if (userRepository.findByUserid(newUserid) != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "이미 존재하는 아이디입니다."));
            }
            user.setUserid(newUserid);
            userRepository.save(user);
            handleLogoutCleanup(currentUserid, resp);

            return ResponseEntity.ok(Map.of(
                    "message", "이메일이 변경되어 로그아웃되었습니다. 다시 로그인해주세요.",
                    "username", user.getUsername(),
                    "userid", user.getUserid()
            ));
        }
        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "message", "회원 정보가 성공적으로 수정되었습니다.",
                "username", user.getUsername(),
                "userid", user.getUserid()
        ));
    }

    @PutMapping("/api/user/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> req,
            Authentication authentication,
            HttpServletResponse response
    ) {
        String userid = authentication.getName();
        User user = userRepository.findByUserid(userid);

        if (user == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }

        String currentPw = req.get("currentPassword");
        String newPw = req.get("newPassword");
        String confirmPw = req.get("confirmPassword");

        if (currentPw == null || newPw == null || confirmPw == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "모든 입력값을 채워주세요."));
        }

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPw, user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "현재 비밀번호가 올바르지 않습니다."));
        }

        // 새 비밀번호 = 확인 비밀번호 동일 체크
        if (!newPw.equals(confirmPw)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "새 비밀번호가 서로 일치하지 않습니다."));
        }

        // 동일한 비밀번호 방지
        if (passwordEncoder.matches(newPw, user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "기존 비밀번호와 다른 비밀번호를 사용해주세요."));
        }

        // 비밀번호 업데이트
        user.setPassword(passwordEncoder.encode(newPw));
        userRepository.save(user);

        // 로그아웃 처리 (토큰, RT 삭제)
        redisUtil.delete("RT:" + userid);
        response.addCookie(new Cookie("access-token", null));
        response.addCookie(new Cookie("userid", null));

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호가 변경되었습니다. 다시 로그인해주세요."
        ));
    }

    @PostMapping("/api/user/profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어 있습니다."));
            }
            // 로그인 사용자 조회
            String userid = authentication.getName();
            User user = userRepository.findByUserid(userid);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "사용자를 찾을 수 없습니다."));
            }
            // 업로드 경로를 절대경로로 지정 (운영/로컬 동일하게 접근 가능)
            String uploadDir = "C:" + File.separator + "HangleUploads" + File.separator + "profile";
            File uploadDirFile = new File(uploadDir);
            Files.createDirectories(uploadDirFile.toPath());

            String filename = user.getUserid() + "_" + System.currentTimeMillis() + ".png";
            File destination = new File(uploadDirFile, filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("[ 업로드 시도 경로 ] : " + uploadDir);
            System.out.println("[ 저장될 파일 ] : " + destination.getAbsolutePath());

            // DB에 상대경로만 저장
            user.setProfileImageUrl("/uploads/profile/" + filename);
            userRepository.save(user);

            // 응답 반환
            Map<String, Object> response = new HashMap<>();
            response.put("profileImageUrl", "/uploads/profile/" + filename);
            response.put("message", "프로필 이미지가 성공적으로 업로드되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류: " + e.getMessage()));
        }
    }

    @PutMapping("/api/user/theme")
    public ResponseEntity<?> updateTheme(@RequestBody Map<String, String> req, Authentication authentication) {
        String userid = authentication.getName();
        User user = userRepository.findByUserid(userid);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }
        String theme = req.get("theme");
        if (!theme.equals("light") && !theme.equals("dark")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "올바른 테마 값이 아닙니다."));
        }
        user.setTheme(theme);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "테마가 저장되었습니다.", "theme", theme));
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 반환합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/api/user/me")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        // 사용자 식별 (JWT에서 userid 가져오기)
        String userid = authentication.getName();
        User user = userRepository.findByUserid(userid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }
        // JSON 응답 데이터 구성
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("userid", user.getUserid());
        data.put("role", user.getRole());
        data.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        data.put("lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
        data.put("introduction", user.getIntroduction());
        data.put("profileImageUrl", user.getProfileImageUrl());
        data.put("theme", user.getTheme());

        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/api/user/delete")
    public ResponseEntity<?> deleteUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        String userid = authentication.getName();
        System.out.println("[회원 탈퇴 요청] 현재 로그인된 사용자: " + userid);

        User user = userRepository.findByUserid(userid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }

        try {
            userRepository.delete(user);
            handleLogoutCleanup(authentication.getName(), response);
            return ResponseEntity.ok(Map.of(
                    "message", "회원 탈퇴 및 로그아웃이 완료되었습니다."
            ));
        } catch (Exception e) {
            log.error("[회원탈퇴 오류]", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "회원 탈퇴 처리 중 오류 발생"));
        }
    }

    private void handleLogoutCleanup(String userid, HttpServletResponse response) {
        // Redis RT 삭제
        redisUtil.delete("RT:" + userid);
        // Access Token 쿠키 제거
        clearCookie(response, JwtProperties.ACCESS_TOKEN_COOKIE_NAME);
        // Userid 쿠키 제거
        clearCookie(response, "userid");
        // SecurityContext 초기화 (세션 강제 해제)
        SecurityContextHolder.clearContext();
    }

    @Operation(summary = "AccessToken 검증", description = "현재 Access Token이 유효한지 확인합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication : " + authentication);

        if (authentication == null) {
            System.out.println("미인증: authentication == null");
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }

        Collection<? extends GrantedAuthority> auth =  authentication.getAuthorities();
        auth.forEach(System.out::println);
        boolean hasRoleAnon = auth.stream()
                .anyMatch(authority -> "ROLE_ANONYMOUS".equals(authority.getAuthority()));

        if (authentication.isAuthenticated() && !hasRoleAnon) {
            System.out.println("인증된 상태입니다. -> " + authentication.getName());
            return new ResponseEntity<>("",HttpStatus.OK);
        }

        System.out.println("미인증된 상태입니다.");
        return new ResponseEntity<>("",HttpStatus.UNAUTHORIZED);
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
