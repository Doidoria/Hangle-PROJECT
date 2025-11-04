package com.example.demo.config.auth.jwt;


import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.JwtTokenRepository;
import com.example.demo.domain.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;



@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {


    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenRepository jwtTokenRepository;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException, IOException {
        System.out.println("[JWTAUTHORIZATIONFILTER] doFilterInternal...");

        // cookie 에서 JWT token을 가져옵니다.
        String token = null;
        String userid = null;

        try {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(JwtProperties.ACCESS_TOKEN_COOKIE_NAME)).findFirst()
                    .map(cookie -> cookie.getValue())
                    .orElse(null);

            userid = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("userid") ).findFirst()
                    .map(cookie -> cookie.getValue())
                    .orElse(null);

        }catch(Exception e){

        }

        if (token != null && userid!=null) {
            try {
                //엑세스 토큰의 유효성체크
                if(jwtTokenProvider.validateToken(token)) {
                    Authentication authentication = getUseridPasswordAuthenticationToken(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("[JWTAUTHORIZATIONFILTER] : " + authentication);
                }

            } catch (ExpiredJwtException e)     //토큰만료시 예외처리(쿠키 제거)
            {
                String refreshToken =  redisUtil.getRefreshToken("RT:"+userid);
                try{
                        if(jwtTokenProvider.validateToken(refreshToken)){
                            //accessToken 만료 o, refreshToken 만료 x -> access-token갱신
                            long now = (new Date()).getTime();
                            User user = userRepository.findByUserid(userid);
                            // Access Token 생성
                            Date accessTokenExpiresIn = new Date(now + JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME); // 60초후 만료
                            String accessToken = Jwts.builder()
                                    .setSubject(userid)
                                    .claim("userid",userid) //정보저장
                                    .claim("auth", user.getRole())//정보저장
                                    .setExpiration(accessTokenExpiresIn)
                                    .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256)
                                    .compact();
                            //클라이언트 전달
                            Cookie cookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME,accessToken);
                            cookie.setMaxAge(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                    }catch(ExpiredJwtException refreshTokenExpiredException){
                        //엑세스토큰 만료 o , 리프레시 토큰 만료 o //클라이언트 만료된 AccessToken 삭제
                        Cookie cookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME,null);
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                        //USERID쿠키도 삭제
                        Cookie userCookie = new Cookie("userid",null);
                        userCookie.setMaxAge(0);
                        userCookie.setPath("/");
                        response.addCookie(userCookie);
                        //REDIS에서 삭제
                        redisUtil.delete("RT:"+userid);
                }
                System.out.println("[JWTAUTHORIZATIONFILTER] : ...ExpiredJwtException ...."+e.getMessage());

            }catch(Exception e2){
                //그외 나머지
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

}