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
    public void createDefaultAccounts() {

        createAccountIfNotExists(
                "admin",
                "관리자",
                "ROLE_ADMIN",
                "admin"
        );

        createAccountIfNotExists(
                "manager",
                "매니저",
                "ROLE_MANAGER",
                "manager"
        );
    }

    private void createAccountIfNotExists(String userid, String username, String role, String password) {

        if (userRepository.findByUserid(userid) != null) {
            System.out.println("[INIT] 이미 존재 → " + userid);
            return;
        }

        User user = User.builder()
                .userid(userid)
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .isCertified(true)
                .build();

        userRepository.save(user);

        System.out.println("[INIT] 기본 계정 생성 완료");
        System.out.println(" ▶ userid : " + userid);
        System.out.println(" ▶ role   : " + role);
    }
}
