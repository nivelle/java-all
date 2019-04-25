package com.nivelle.programming.springboot.controllor;

import com.nivelle.programming.springboot.configbean.LearnConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("config")
public class HelloController {

    @Autowired
    LearnConfig learnConfig;

    @RequestMapping("/hello")
    public String hello() {

        String desc = learnConfig.getDesc();

        return "hello world my name is " + desc;
    }
}
