package com.example.demo.domain.user.repository;

import com.example.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // userid (이메일 또는 아이디)로 조회
    User findByUserid(String userid);

    // username으로 조회해야 할 필요 있을 경우
    Optional<User> findByUsername(String username);
}
