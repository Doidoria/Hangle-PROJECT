package com.example.demo.config.auth.scheduled;

import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.jwt.KeyGenerator;
import com.example.demo.domain.entity.Signature;
import com.example.demo.domain.repository.SignatureRepository;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@EnableScheduling
public class SignatureScheduling {
    @Autowired
    private SignatureRepository signatureRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Scheduled(cron = "0 0 * * * *") //초 분 시 일 월 요일(0-6)
    public void signatureScheduled(){
        List<Signature> list = signatureRepository.findAll(); //1개 값만 저장되어있음
        if(!list.isEmpty()){
            signatureRepository.deleteAll(); //기존 서명키 제거
        }
        //KENGEN으로 서명에 사용할 KEY배열 생성
        byte[] keyBytes = KeyGenerator.getKeygen();
        //새로운 서명키 발급  + DB 저장
        Signature newSignature = new Signature();
        newSignature.setKeyBytes(keyBytes);
        newSignature.setCreateAt(LocalDate.now());
        signatureRepository.save(newSignature);
        //JWTTOKENPROVIDER에 새로 발급받은 KEY전달
        jwtTokenProvider.setKey(Keys.hmacShaKeyFor(keyBytes));
        System.out.println("SignatureScheduling Sinature Update...");
    }
}
