package com.example.demo.domain.user.service.impl;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements AppUserService {

    private final UserRepository userRepository;

    @Override
    public User findByUserid(String userid) {
        User user = userRepository.findByUserid(userid);
        if (user == null) {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다: " + userid);
        }
        return user;
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: id=" + id));
    }
}
