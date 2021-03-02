package com.nivelle.spring.springboot.mystarter;

import com.nivelle.starter.MyStarterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class StarterTest {

    @Resource
    private MyStarterService myStarterService;

    @GetMapping("/say")
    public String sayWhat() {
        return myStarterService.say();
    }

}
