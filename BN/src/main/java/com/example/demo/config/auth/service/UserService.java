package com.example.demo.config.auth.service;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자의 고유 username을 변경합니다.
     * * @param userId 현재 로그인된 사용자의 고유 ID (PK)
     * @param newUsername 새로 설정할 사용자 이름
     * @return 업데이트된 User 엔티티
     */
    @Transactional
    public User updateUsername(String userId, String newUsername) {

        // 1. 새로운 username 중복 확인
        userRepository.findByUsername(newUsername).ifPresent(existingUser -> {
            // 다른 사용자가 이미 사용 중인 경우에만 예외 발생
            if (!existingUser.getUserid().equals(userId)) {
                throw new IllegalArgumentException("Username '" + newUsername + "' is already taken.");
            }
        });

        // 2. 현재 사용자 조회
        // UserRepository의 findByUserid(String)이 User를 반환한다고 가정
        User user = userRepository.findByUserid(userId);

        if (user == null) {
            throw new NoSuchElementException("User not found with ID: " + userId);
        }

        // 3. 사용자 이름 변경
        user.setUsername(newUsername);

        // 4. 변경사항 저장
        return userRepository.save(user);
    }
}