package com.example.demo.domain.service;

import com.example.demo.domain.dto.CompDto;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.Comp;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;

    public Long compRegisstrarion1(UserDto dto) throws Exception{
        //dto -> entity
        User user = User.builder()
                .userId(dto.getUserId())
                .userName(dto.getUserName())
                .build();
        userRepository.save(user);
        return user.getUserId();
    }






}



