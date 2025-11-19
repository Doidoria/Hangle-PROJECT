package com.example.demo.config;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void createAdminAccount() {

        String adminid = "admin";

        // 이미 admin 계정 있으면 패스
        if (userRepository.findByUserid(adminid) != null) {
            System.out.println("[ADMIN INIT] Admin 계정 이미 존재함");
            return;
        }

        // 관리자 생성
        User admin = User.builder()
                .userid(adminid)
                .username("관리자")
                .password(passwordEncoder.encode("admin"))
                .role("ROLE_ADMIN")
                .isCertified(true)
                .build();

        userRepository.save(admin);

        System.out.println("[ADMIN INIT] 기본 관리자 계정 생성 완료!");
        System.out.println("   ▶ ID : " + adminid);
        System.out.println("   ▶ PW : admin");
    }
}
