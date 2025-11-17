package com.example.demo.config.auth.jwt;


import com.example.demo.config.auth.service.PrincipalDetails;
import com.example.demo.domain.user.dto.UserDto;
import com.example.demo.domain.user.entity.Signature;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.SignatureRepository;
import com.example.demo.domain.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

import java.security.Key;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SignatureRepository signatureRepository;
    //Key 저장
    private Key key;
    public void setKey(Key key){
        this.key = key;
    }
    public Key getKey(){
        return this.key;
    }

    //SIGNATURE 저장
    @EventListener(ApplicationReadyEvent.class)
    public void init(){
        List<Signature> list = signatureRepository.findAll(); //1개 값만 저장되어있음
        if(list.isEmpty()){
            //처음 SIGNATURE발급
            byte[] keyBytes = KeyGenerator.getKeygen();
            this.key = Keys.hmacShaKeyFor(keyBytes);
            Signature signature = new Signature();
            signature.setKeyBytes(keyBytes);
            signature.setCreateAt(LocalDate.now());
            signatureRepository.save(signature);
            System.out.println("JwtTokenProvider init()  Key init : " + key);
        }else{
            //기존 SIGNATURE이용
            Signature signature = list.get(0);
            this.key = Keys.hmacShaKeyFor(signature.getKeyBytes());
            System.out.println("JwtTokenProvider init()  기존 Key 사용 : " + key);
        }
    }
    public JwtTokenProvider() {

    }

    // 유저 정보를 가지고 AccessToken, RefreshToken 을 생성하는 메서드
    public TokenInfo generateToken(Authentication authentication) {
        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();

        // role 클레임 추가
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // JWT Claims 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("userid", authentication.getName());
        claims.put("role", role);         // 핵심 추가 (ROLE_USER / ROLE_ADMIN 등)
        claims.put("auth", authorities);  // 기존 auth 필드도 유지

        // Access Token 생성 (여기서 받아서 user 정보 넘겨도 됨)
        Date accessTokenExpiresIn = new Date(now + JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME); // 60초후 만료
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .addClaims(claims)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("userid", authentication.getName())
                .setExpiration(new Date(now + JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("JwtTokenProvider.generateToken.accessToken : " + accessToken);
        System.out.println("JwtTokenProvider.generateToken.refreshToken : " + refreshToken);

        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        List<GrantedAuthority> authorities = new ArrayList<>();

        // auth 클레임이 있다면
        if (claims.get("auth") != null) {
            authorities.addAll(
                    Arrays.stream(claims.get("auth").toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList())
            );
        }

        // role 클레임이 있다면 추가 (중복 방지)
        if (claims.get("role") != null) {
            String role = claims.get("role").toString();
            if (authorities.stream().noneMatch(a -> a.getAuthority().equals(role))) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }

        String userid = claims.getSubject(); //userid

        // PrincipalDetails 생성
        User user = userRepository.findByUserid(userid);
        UserDto userDto = null;
        if (user == null) {
            throw new UsernameNotFoundException("아이디가 존재하지 않습니다.");
        }
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        System.out.println("JwtTokenProvider.getAuthentication UseridPasswordAuthenticationToken : " + accessToken);
        UsernamePasswordAuthenticationToken useridPasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(principalDetails, "", authorities);
        return useridPasswordAuthenticationToken;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) throws ExpiredJwtException{
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        }
        catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            throw e;

        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public Date getExpiration(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getExpiration();
    }

}