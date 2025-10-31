package com.example.hangle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // ✅ 로그인 페이지 연결
    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html"; // static 폴더의 login.html로 직접 연결
    }

    // ✅ 홈 페이지 연결
    @GetMapping({"/", "/index"})
    public String indexPage() {
        return "forward:/index.html"; // static 폴더의 index.html로 연결
    }
}
