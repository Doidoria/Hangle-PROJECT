package com.example.demo.controller;

import com.example.demo.domain.dto.PersonDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/param")
@CrossOrigin(origins={"http://localhost:3000","http://127.0.0.1:3000"})
public class ParamController {

    @RequestMapping(value="/01",method={RequestMethod.GET,RequestMethod.POST} , produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity< Map<String,Object>  > param01(@RequestParam(value="name",required = false)String name){
        Map<String,Object> map = new LinkedHashMap<>();
        log.info("GET/POST /param/01..." + name);

        map.put("name",name);
        map.put("status","success");

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @RequestMapping(value="/02/{name}/{age}/{addr}",method={RequestMethod.GET,RequestMethod.POST},produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity< Map<String,Object>  > param02(PersonDto personDto){
        Map<String,Object> map = new LinkedHashMap<>();
        log.info("GET /param/02..." + personDto);

        map.put("name",personDto.getName());
        map.put("age",personDto.getAge());
        map.put("addr",personDto.getAddr());

        return personDto==null ? new ResponseEntity<>(null,HttpStatus.EXPECTATION_FAILED) : new ResponseEntity<>(map,HttpStatus.OK);
    }

    @PostMapping(value="/03",consumes = MediaType.APPLICATION_JSON_VALUE,produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity< Map<String,Object> > param03(@RequestBody PersonDto personDto){
        Map<String,Object> map = new LinkedHashMap<>();
        log.info("GET /param/03...");
        map.put("name",personDto.getName());
        map.put("age",personDto.getAge());
        map.put("addr",personDto.getAddr());
        map.put("status","success");

        return personDto==null ? new ResponseEntity<>(null,HttpStatus.EXPECTATION_FAILED) : new ResponseEntity<>(map,HttpStatus.OK);
    }
}
