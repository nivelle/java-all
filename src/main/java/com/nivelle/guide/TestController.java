package com.nivelle.guide;

import com.nivelle.guide.datastructures.Son;
import com.nivelle.guide.springboot.configbean.LearnConfig;
import com.nivelle.guide.springboot.mapper.ActivityPvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    LearnConfig learnConfig;
    @Autowired
    ActivityPvMapper activityPvMapper;

    @RequestMapping("/config")
    public String config() {

        String desc = learnConfig.getDesc();

        Son man = new Son(1, "nivelle", 100);

        System.out.println(desc);

        System.out.println(man.getScore() + man.getName() + man.getAge());

        return "hello world my name is " + desc;
    }

    @RequestMapping("/extends")
    @ResponseBody
    public String hello() {

        Son man = new Son(1, "nivelle", 100);
        System.out.println(man.getScore() + man.getName() + man.getAge());
        return "class extends name is:" + man.getName() +" "+"score is: "+ man.getScore();
    }

}
