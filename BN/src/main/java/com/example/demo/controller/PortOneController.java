package com.example.demo.controller;

import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.config.auth.service.PrincipalDetails;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/portOne")
public class PortOneController {

    private final RedisUtil redisUtil;
    private final UserRepository userRepository;

    private final String HOSTNAME = "https://api.iamport.kr";
    private final String APIKEY = "7125401617111610";
    private final String SECRET = "HPzom4PG2vUc7KG8WUn1RJSJEkL9QLh0yf6zNanfefU3LwcRGOABGrZ2iNSzPMp367L4HQeXhThdLRVD";
    private final String REDIS_TOKEN_KEY = "portone:access_token";

    // PORTONE ACCESSTOKEN 응답 구조를 위한 DTO
    @Data
    private static class Item {
        @JsonProperty("access_token")
        public String accessToken;
        public long now;
        @JsonProperty("expired_at")
        public long expiredAt;
    }

    @Data
    private static class PortOneTokenResponse {
        public int code;
        public String message;
        @JsonProperty("response")
        public Item item;
    }

    // Access Token 획득 메서드 (로직 동일)
    private String getAccessToken() {
        String cachedToken = redisUtil.getStringData(REDIS_TOKEN_KEY);
        if (cachedToken != null) {
            log.info("PortOne AccessToken을 Redis 캐시에서 사용합니다.");
            return cachedToken;
        }

        String url = HOSTNAME + "/users/getToken";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        JSONObject params = new JSONObject();
        params.put("imp_key", APIKEY);
        params.put("imp_secret", SECRET);
        HttpEntity<String> entity = new HttpEntity<>(params.toJSONString(), header);

        try {
            ResponseEntity<PortOneTokenResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, PortOneTokenResponse.class
            );

            PortOneTokenResponse tokenResponse = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && tokenResponse != null && tokenResponse.getCode() == 0) {
                Item tokenItem = tokenResponse.getItem();
                String newToken = tokenItem.getAccessToken();
                long expiresAt = tokenItem.getExpiredAt();
                long now = tokenItem.getNow();

                long expirationSeconds = expiresAt - now - 60;

                if (expirationSeconds > 0) {
                    redisUtil.setDataExpire(REDIS_TOKEN_KEY, newToken, expirationSeconds);
                } else {
                    log.warn("PortOne AccessToken의 만료 시간이 유효하지 않습니다. ({}초)", expirationSeconds);
                }

                log.info("PortOne AccessToken을 새로 발급받아 Redis에 저장했습니다. (만료: {}초)", expirationSeconds);
                return newToken;
            } else {
                log.error("PortOne AccessToken 발급 API 응답 실패. Code: {}, Message: {}",
                        tokenResponse != null ? tokenResponse.getCode() : "N/A",
                        tokenResponse != null ? tokenResponse.getMessage() : "No Body");
            }
        } catch (Exception e) {
            log.error("PortOne AccessToken 발급 API 통신 오류 발생", e);
        }
        return null;
    }


    // PortOne 인증 결과와 DB에 저장된 사용자의 휴대폰 번호를 비교하여 최종 인증을 완료합니다.
    @GetMapping("/certifications/{imp_uid}")
    public ResponseEntity<Map<String, Object>> certifications(@PathVariable("imp_uid") String imp_uid) {

        // 1. 사용자 인증 정보 확인 및 DB 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return new ResponseEntity<>(Map.of("isVerified", false, "message", "로그인 상태가 아닙니다."), HttpStatus.FORBIDDEN);
        }

        String currentUserId;
        try {
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            currentUserId = principalDetails.getUsername();
        } catch (Exception e) {
            log.error("PrincipalDetails 캐스팅 오류 또는 인증 객체 문제", e);
            return new ResponseEntity<>(Map.of("isVerified", false, "message", "인증 객체를 가져오는 중 오류가 발생했습니다."), HttpStatus.FORBIDDEN);
        }

        User user = userRepository.findByUserid(currentUserId);

        if (user == null) {
            return new ResponseEntity<>(Map.of("isVerified", false, "message", "DB에서 사용자 정보를 찾을 수 없습니다."), HttpStatus.FORBIDDEN);
        }
        String userPhone = user.getPhone();
        if (userPhone == null || userPhone.isBlank()) {
            return new ResponseEntity<>(Map.of("isVerified", false, "message", "회원정보에 등록된 휴대폰 번호가 없습니다. 등록 후 재시도하세요."), HttpStatus.BAD_REQUEST);
        }

        // 2. AccessToken 획득 및 PortOne API 조회
        String accessToken = getAccessToken();
        if (accessToken == null) {
            return new ResponseEntity<>(Map.of("isVerified", false, "message", "PortOne Access Token 획득 실패"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String url = HOSTNAME + "/certifications/" + imp_uid;
        HttpHeaders header = new HttpHeaders();
        header.add("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(header);
        RestTemplate rt = new RestTemplate();

        Map<String, Object> result = new HashMap<>();
        result.put("isVerified", false);

        try {
            ResponseEntity<String> response = rt.exchange(url, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();

            // PortOne API에서 받은 원본 응답 로그 출력 (디버깅용)
            log.info("PortOne API 응답 (imp_uid: {}): {}", imp_uid, responseBody);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = (JSONObject) new JSONParser().parse(responseBody);

                if (jsonResponse.get("code") instanceof Long && (Long) jsonResponse.get("code") == 0L) {
                    JSONObject responseData = (JSONObject) jsonResponse.get("response");

                    // response 객체가 null인지 확인 (인증 정보가 없을 경우 대비)
                    if (responseData == null) {
                        result.put("message", "PortOne으로부터 해당 imp_uid의 인증 정보를 찾을 수 없습니다. (응답 데이터 Null)");
                        log.warn("PortOne 인증 정보 없음: imp_uid={}", imp_uid);
                        return new ResponseEntity<>(result, HttpStatus.OK);
                    }

                    String status = (String) responseData.get("status");
                    Boolean certified = (Boolean) responseData.get("certified"); // certified 필드 추가

                    // 성공 조건 변경: status가 "identified"이거나, status가 null/missing 이지만 certified가 true인 경우
                    if ("identified".equals(status) || Boolean.TRUE.equals(certified)) {
                        String certifiedPhone = (String) responseData.get("phone");

                        if (userPhone.equals(certifiedPhone)) {
                            result.put("isVerified", true);
                            result.put("message", "본인 인증 및 휴대폰 번호 확인 완료");

                            user.setIsCertified(true);
                            userRepository.save(user);
                            log.info("{} 사용자의 휴대폰 인증 성공", currentUserId);

                        } else {
                            result.put("message", "인증된 휴대폰 번호가 회원정보와 일치하지 않습니다.");
                            log.warn("{} 사용자의 휴대폰 인증 실패 (번호 불일치)", currentUserId);
                        }
                    } else {
                        // 실패 상태를 명확히 로그에 기록 (status가 있다면 status를 사용, 없다면 certified 상태를 사용)
                        String failureStatus = status != null ? status :
                                (certified != null ? "certified:" + certified : "Unknown Status");

                        log.warn("PortOne 인증 상태 실패 (imp_uid: {}): Status={}", imp_uid, failureStatus);
                        result.put("message", "PortOne 인증 상태 실패: " + failureStatus);
                    }
                } else {
                    result.put("message", "PortOne API 응답 오류: " + jsonResponse.get("message"));
                    log.error("PortOne API 응답 코드 오류: Code={}, Message={}", jsonResponse.get("code"), jsonResponse.get("message"));
                }
            } else {
                result.put("message", "PortOne API 통신 실패: HTTP 상태 코드 " + response.getStatusCode());
                log.error("PortOne API 통신 실패: HTTP Status={}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("인증 조회 중 예외 발생 (imp_uid: {})", imp_uid, e);
            result.put("message", "서버 처리 중 예외 발생");
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}