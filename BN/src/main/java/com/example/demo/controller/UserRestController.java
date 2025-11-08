package com.example.demo.controller;

import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.jwt.TokenInfo;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.user.dto.UserDto;
import com.example.demo.domain.user.repository.JwtTokenRepository;
import com.example.demo.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
public class UserRestController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private RedisUtil redisUtil;

    @PostMapping(value = "/join",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> join_post(@RequestBody UserDto userDto){
        log.info("POST /join..."+userDto);

        // 비밀번호 일치 검사
        if (!userDto.getPassword().equals(userDto.getRepassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "패스워드가 일치하지 않습니다."));
        }

        // 사용자 존재 여부 검사
        User existingUser = userRepository.findByUserid(userDto.getUserid());
        if (existingUser != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미 존재하는 사용자입니다."));
        }

        //dto -> entity
        User user = userDto.toEntity();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // save entity to DB
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }
    //Header 방식 (Authorization: Bearer <token>)
    // - XXS 공격에 매우취약 - LocalStorage / SessionStorage에 저장시 문제 발생
    // - 쿠키방식이 비교적 안전
    @PostMapping(value = "/login" , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> login(@RequestBody UserDto userDto, HttpServletResponse resp) throws IOException {
        log.info("POST /login..." + userDto);
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByUserid(userDto.getUserid());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "존재하지 않는 사용자입니다."));
        }

        // 비밀번호 일치 확인
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

    @GetMapping("/user")
    public ResponseEntity< Map<String,Object> > user(HttpServletRequest request, Authentication authentication) {
        log.info("GET /user..." + authentication);
        log.info("name..." + authentication.getName());

        Optional<User> userOptional =  userRepository.findById(authentication.getName());
        // Access토큰에 정보를 넣어서 authentication으로 바로 꺼내와도 됨.
        Map<String, Object> response = new HashMap<>();

        if(userOptional.isPresent()){
            User user = userOptional.get();
            response.put("userid",user.getUserid());
            response.put("username",user.getUsername());
            response.put("role",user.getRole());

            return new ResponseEntity<>(response , HttpStatus.OK);
        }
        return new ResponseEntity<>(null , HttpStatus.UNAUTHORIZED);
    }

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
