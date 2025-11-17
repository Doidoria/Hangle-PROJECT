package com.example.demo.config.auth.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // Redis에서 Refresh Token 가져오기(key 예시 JwtToken:user1 , JwtToken:user2...)
    public String getRefreshToken(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 데이터 삭제
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 추가: 만료 시간과 함께 데이터 저장
    public void setDataExpire(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, duration, TimeUnit.SECONDS);
    }

    // 추가: Map 데이터를 JSON 문자열로 변환하여 저장
    public void setDataExpire(String key, Map<String, String> value, long duration) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, duration, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to JSON", e);
        }
    }

    // 추가: 데이터 조회 및 Map으로 변환
    @SuppressWarnings("unchecked")
    public Map<String, String> getData(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.readValue(value, Map.class);
        } catch (Exception e) {
            // JSON 파싱 실패 시 null 반환
            return null;
        }
    }
    // 추가: 문자열 데이터 조회
    public String getStringData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 추가: 키 존재 여부 확인
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}