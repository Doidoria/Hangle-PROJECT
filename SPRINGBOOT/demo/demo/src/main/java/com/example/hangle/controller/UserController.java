package com.example.hangle.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    // ✅ 현재 로그인한 사용자명 반환
    @GetMapping("/whoami")
    public Object whoAmI(Authentication authentication) {
        if (authentication == null) {
            return "anonymous";
        }
        return authentication.getPrincipal();
    }
}
