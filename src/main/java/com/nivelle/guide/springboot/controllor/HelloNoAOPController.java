package com.nivelle.guide.springboot.controllor;

import com.nivelle.guide.springboot.configbean.LearnConfig;
import com.nivelle.guide.springboot.mapper.ActivityPvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("configNoAOP")
public class HelloNoAOPController {

    @Autowired
    LearnConfig learnConfig;
    @Autowired
    ActivityPvMapper activityPvMapper;

    @RequestMapping("/hello")
    public String hello() {

        String desc = learnConfig.getDesc();

        return "hello world my name is " + desc;
    }

}
