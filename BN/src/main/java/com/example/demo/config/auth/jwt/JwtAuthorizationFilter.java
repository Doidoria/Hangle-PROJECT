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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    @PostConstruct
    public void init() {
        System.out.println("!!! JwtAuthorizationFilter Bean 등록됨!");
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
            chain.doFilter(request, response);
            return;
        }

        // -------------------------------
        // 2) 🔥 제출 API 우회 (JWT 필터 스킵)
        // -------------------------------
        if (uri.matches("^/api/competitions/\\d+/submit$")) {
            chain.doFilter(request, response);
            return;
        }

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
        } catch (Exception e) {
            System.out.println("[JWT] 쿠키 파싱 중 예외: " + e.getMessage());
        }

        try{
            if (token != null && userid != null) {
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        Authentication authentication = jwtTokenProvider.getAuthentication(token);
                        if (authentication != null) {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (ExpiredJwtException e) {
                    System.out.println("[JWT] AccessToken 만료 → RefreshToken 확인 시작");
                    if (userid == null) {
                        try {
                            userid = e.getClaims().getSubject();
                        } catch (Exception ex) {
                            System.out.println("[JWT] 만료 토큰에서 userid 추출 실패");
                        }
                    }
                    String refreshToken = redisUtil.getRefreshToken("RT:" + userid);

                    // refreshToken 없음
                    if (refreshToken == null) {
                        clearAuthCookies(response);
                        redisUtil.delete("RT:" + userid);
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        System.out.println("[JWT] RT 없음 → 로그아웃 처리");
                        return;
                    }
                    try {
                        if (jwtTokenProvider.validateToken(refreshToken)) {
                            long now = System.currentTimeMillis();
                            User user = userRepository.findByUserid(userid);
                            Date accessTokenExpiresIn = new Date(now + JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME);

                            String newAccessToken = Jwts.builder()
                                    .setSubject(userid)
                                    .claim("userid", userid)
                                    .claim("auth", user.getRole())
                                    .setExpiration(accessTokenExpiresIn)
                                    .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256)
                                    .compact();

                            Cookie cookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, newAccessToken);
                            cookie.setHttpOnly(true);
                            cookie.setSecure(false); // 로컬 테스트용
                            cookie.setPath("/");
                            cookie.setMaxAge((int) (JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME / 1000));
                            response.addCookie(cookie);

                            // SameSite 방지 (브라우저에 저장 안 되는 경우)
                            response.setHeader("Set-Cookie",
                                    String.format("%s=%s; Path=/; HttpOnly; SameSite=Lax",
                                            JwtProperties.ACCESS_TOKEN_COOKIE_NAME, newAccessToken));

                            // 슬라이딩 세션: RT 잔여시간 짧으면 갱신
                            long rtExpireTime = jwtTokenProvider.getExpiration(refreshToken).getTime();
                            long rtRemaining = rtExpireTime - now;
                            if (rtRemaining < 1000L * 60 * 5) { // 5분 미만이면 회전
                                String newRefreshToken = Jwts.builder()
                                        .setSubject(userid)
                                        .claim("userid", userid)
                                        .setExpiration(new Date(now + JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME))
                                        .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256)
                                        .compact();

                                // RT 회전(슬라이딩 세션) 시
                                redisUtil.setDataExpire("RT:" + userid, newRefreshToken,
                                        JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME / 1000);
                                System.out.println("[JWT] RefreshToken 회전 완료");
                            }

                            // SecurityContext 갱신
                            Authentication authentication = jwtTokenProvider.getAuthentication(newAccessToken);
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            System.out.println("[JWT] AccessToken 재발급 완료 → " + userid);

                            chain.doFilter(request, response);
                            return;
                        } else {
                            clearAuthCookies(response);
                            redisUtil.delete("RT:" + userid);
                            SecurityContextHolder.clearContext();
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            System.out.println("[JWT] RefreshToken 만료 → 로그아웃 처리");
                            return;
                        }
                    } catch (ExpiredJwtException ex) {
                        clearAuthCookies(response);
                        redisUtil.delete("RT:" + userid);
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        System.out.println("[JWT] RefreshToken 만료 → 로그아웃 처리");
                        return;
                    }
                } catch (Exception e2) {
                    System.out.println("[JWT] 기타 예외 발생: " + e2.getMessage());
                    clearAuthCookies(response);
                    redisUtil.delete("RT:" + userid);
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
            chain.doFilter(request, response);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        Cookie access = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, null);
        access.setPath("/");
        access.setHttpOnly(true);
        access.setSecure(false);
        access.setMaxAge(0);

        Cookie user = new Cookie("userid", null);
        user.setPath("/");
        user.setHttpOnly(true);
        user.setSecure(false);
        user.setMaxAge(0);

        response.addCookie(access);
        response.addCookie(user);
    }
}
