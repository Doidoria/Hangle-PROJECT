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
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@Slf4j
@RequiredArgsConstructor
public class UserRestController {

    //    @Autowired
    final private UserRepository userRepository;
    //    @Autowired
    final private PasswordEncoder passwordEncoder;
    //    @Autowired
    final private AuthenticationManager authenticationManager;
    //    @Autowired
    final private JwtTokenProvider jwtTokenProvider;
    //    @Autowired
    final private RedisUtil redisUtil;

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

        return ResponseEntity.ok(data);
    }

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

        try{
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

            Cookie accessCookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, tokenInfo.getAccessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false); // Only for HTTPS
            accessCookie.setPath("/"); // Define valid paths
            accessCookie.setMaxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME); // 1 hour expiration

            Cookie userCookie = new Cookie("userid", authentication.getName());
            userCookie.setHttpOnly(true);
            userCookie.setSecure(false); // Only for HTTPS
            userCookie.setPath("/");
            userCookie.setMaxAge(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME); // 7 days expiration

            resp.addCookie(accessCookie);
            resp.addCookie(userCookie);

        }catch(AuthenticationException e){
            System.out.println("인증실패 : " + e.getMessage());
            response.put("state","fail");
            response.put("message","아이디 또는 비밀번호가 올바르지 않습니다.");
            return new ResponseEntity(response,HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @Operation(summary = "AccessToken 검증", description = "현재 Access Token이 유효한지 확인합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication : " + authentication);
        Collection<? extends GrantedAuthority> auth =  authentication.getAuthorities();
        auth.forEach(System.out::println);
        boolean hasRoleAnon = auth.stream()
                // 기본 롤이 ROLE_ANONYMOUS 상태라서 로그인 상태가 아니라고 판단
                .anyMatch(authority -> "ROLE_ANONYMOUS".equals(authority.getAuthority()));

        if (authentication.isAuthenticated() && !hasRoleAnon) {
            System.out.println("인증된 상태입니다.");
            return new ResponseEntity<>("",HttpStatus.OK);
        }

        System.out.println("미인증된 상태입니다.");
        return new ResponseEntity<>("",HttpStatus.UNAUTHORIZED);
    }
}
