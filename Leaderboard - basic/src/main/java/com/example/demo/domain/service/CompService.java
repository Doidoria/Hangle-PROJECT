package com.example.demo.domain.service;


import com.example.demo.domain.dto.CompDto;

import com.example.demo.domain.entity.Comp;
import com.example.demo.domain.repository.CompRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompService {


    @Autowired
    private CompRepository compRepository;

    public Long compRegisstrarion1(CompDto dto) throws Exception{
        //dto -> entity
       Comp comp = Comp.builder()
               .compId(null)
               .compName(dto.getCompName())
                .build();
        compRepository.save(comp);
        return comp.getCompId();
    }



    public List<CompDto> getAllCompList() {
        System.out.println("compList");
        return compRepository.findAll()
                .stream()
                .map(comp -> new CompDto(
                        comp.getCompId(),
                        comp.getCompName()
                ))
                .collect(Collectors.toList());
    }




}



