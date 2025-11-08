package com.example.demo.controller;

import com.example.demo.domain.dto.CompDto;
import com.example.demo.domain.dto.LeaderboardDto;
import com.example.demo.domain.service.CompService;
import com.example.demo.domain.service.LeaderboardService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/comp")
public class CompController {

    @Autowired
    private CompService compService;

    @Autowired
    private LeaderboardService leaderboardService;



    @GetMapping("/add")
    public void comp_add ()throws Exception {
        log.info("GET /comp/add...");
    }
    @PostMapping("/add")
    public String comp_add_post(
            @Valid CompDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            LeaderboardDto dto2
    ) throws Exception{
       if(bindingResult.hasErrors()){
           for(FieldError error : bindingResult.getFieldErrors())
           {
               log.info("Error Field : "+ error.getField());
               model.addAttribute(error.getField(),error.getDefaultMessage());
           }
           return "comp/add";
       }

       //서비스 연결
        Long insertedId = compService.compRegisstrarion1(dto);
       if(insertedId != null){
           redirectAttributes.addFlashAttribute("message" ,"대회 등록 완료!" + insertedId );
       }



        return insertedId != null? "redirect:/comp/list":"comp/add";
    }

    @GetMapping("/list")
    public String Comp_List(

            Model model) throws Exception{

        log.info("comp list");
        List<CompDto> list = compService.getAllCompList();
        System.out.println("compList size = " + list.size());
        model.addAttribute("compList", list);
        return "comp/list";
    }


}
