package com.example.demo.domain.user.service;

import com.example.demo.domain.user.entity.User;

public interface AppUserService {

    // userid 로 User 조회
    User findByUserid(String userid);

    // id 로 User 조회
    User findById(Long id);
}
