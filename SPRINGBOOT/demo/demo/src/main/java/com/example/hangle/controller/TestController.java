package com.example.hangle.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.hangle.config.auth.PrincipalDetails;

@Controller
public class TestController {

//    @GetMapping("/whoami")
//    public String whoAmI(@AuthenticationPrincipal PrincipalDetails user) {
//        System.out.println("현재 로그인 사용자: " + user.getUsername());
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("Authentication 객체: " + auth);
//        return "forward:/index.html";  // 확인 후 index로 이동
//    }
}
