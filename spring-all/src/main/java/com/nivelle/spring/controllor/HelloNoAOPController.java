package com.nivelle.spring.controllor;

import com.nivelle.spring.configbean.CommonConfig;
import com.nivelle.spring.springboot.mapper.ActivityPvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test/configNoAOP")
public class HelloNoAOPController {

    @Autowired
    CommonConfig commonConfig;
    @Autowired
    ActivityPvMapper activityPvMapper;

    @RequestMapping("/hello")
    public String hello() {

        String desc = commonConfig.getDesc();

        return "hello world my name is " + desc;
    }

}
