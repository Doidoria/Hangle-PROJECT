package com.example.demo.config.auth.jwt;

import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    @PostConstruct
    public void init() {
        System.out.println("!!! JwtAuthorizationFilter Bean 등록됨! logger = " + this.logger);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String uri = request.getRequestURI();
        if (uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/swagger-resources")
                || uri.equals("/swagger-ui.html")
                || request.getMethod().equalsIgnoreCase("OPTIONS")) {
            System.out.println("[JWT] Swagger 요청 감지 → 필터 완전 스킵");
            chain.doFilter(request, response);
            return;
        }

        // cookie 에서 JWT token을 가져옵니다.
        String token = null;
        String userid = null;

        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                token = Arrays.stream(cookies)
                        .filter(c -> JwtProperties.ACCESS_TOKEN_COOKIE_NAME.equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);

                userid = Arrays.stream(cookies)
                        .filter(c -> "userid".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }

        }catch(Exception e){

        }

        if (token != null && userid!=null) {
            try {
                //엑세스 토큰의 유효성체크
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    if (authentication != null) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }

            } catch (ExpiredJwtException e)     //토큰만료시 예외처리(쿠키 제거)
            {
                String refreshToken = redisUtil.getRefreshToken("RT:" + userid);
                if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                } else {
                    // RefreshToken도 만료 or 존재 안 함
                    clearAuthCookies(response);
                    redisUtil.delete("RT:" + userid);
                    System.out.println("[JWT] refreshToken 없음 → 로그아웃 처리");
                }
                try{
                        if(jwtTokenProvider.validateToken(refreshToken)){
                            //accessToken 만료 o, refreshToken 만료 x -> access-token갱신
                            long now = (new Date()).getTime();
                            User user = userRepository.findByUserid(userid);
                            Date accessTokenExpiresIn = new Date(now + JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME);
                            String accessToken = Jwts.builder()
                                    .setSubject(userid)
                                    .claim("userid", userid)
                                    .claim("auth", user.getRole())
                                    .setExpiration(accessTokenExpiresIn)
                                    .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256)
                                    .compact();
                            //클라이언트 전달
                            Cookie cookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
                            cookie.setHttpOnly(true);
                            cookie.setSecure(false);
                            cookie.setPath("/");
                            cookie.setMaxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME);
                            response.addCookie(cookie);

                            Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            System.out.println("[JWT] AccessToken refreshed for user: " + userid);
                            return; // 여기서 끝내야 다음 필터에서 인증 누락 안 됨
                        }
                    }catch (ExpiredJwtException ex) {
                    clearAuthCookies(response);
                    redisUtil.delete("RT:" + userid);
                    System.out.println("[JWT] RefreshToken 만료 → 로그아웃 처리");
                }
                System.out.println("[JWTAUTHORIZATIONFILTER] : ...ExpiredJwtException ...."+e.getMessage());

            }catch(Exception e2){
                System.out.println("[JWT] 기타 예외 발생: " + e2.getMessage());
            }
        }
        chain.doFilter(request, response);
    }

    // TOKEN -> AUTHENTICATION 변환
    private Authentication getUseridPasswordAuthenticationToken(String token) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        Optional<User> user = userRepository.findById(authentication.getName()); // 유저를 유저명으로 찾습니다.
        System.out.println("JwtAuthorizationFilter.getUseridPasswordAuthenticationToken...authenticationToken : " +authentication );
        if(user.isPresent())
            return authentication;
        return null; // 유저가 없으면 NULL
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie access = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, null);
        access.setPath("/");
        access.setHttpOnly(true);
        access.setSecure(false);
        access.setMaxAge(0);
//        access.setAttribute("SameSite", "None");
        Cookie user = new Cookie("userid", null);
        user.setPath("/");
        user.setHttpOnly(true);
        user.setSecure(false);
        user.setMaxAge(0);
//        user.setAttribute("SameSite", "None");

        response.addCookie(access);
        response.addCookie(user);
    }

}