package com.nivelle.spring;

//import com.nivelle.programming.springboot.mystarter.StarterTest;
//import org.junit.Test;

import com.nivelle.spring.springlife.initdemo.InitSpringBean;
import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SptingTest {

    @Autowired
    InitSpringBean initSpringBean;


    /**
     * spring 钩子方法测试
     *
     * @return
     */
    @RequestMapping("/init")
    public Object myInitSpringBean() {
        String name = initSpringBean.getName();
        int age = initSpringBean.getAge();
        return name + age;
    }
}
