package com.nivelle.guide.springboot.controllor;

import com.google.common.collect.Lists;
import com.nivelle.guide.springboot.hock.MyFactoryBean;
import com.nivelle.guide.springboot.pojo.TimeLine;
import com.nivelle.guide.springboot.hock.SpringContextAssisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("time")
public class TimeLineController {


    @Autowired
    private MyFactoryBean myFactoryBean;

    @Autowired
    private SpringContextAssisor springContextAssisor;

    @Autowired
    private ApplicationContext applicationContext;


    @RequestMapping("/timeline")
    public List<Object> getObject() throws Exception {
        //直接通过#getObject获取实例
        TimeLine timeLine = myFactoryBean.getObject();

        //通过Spring上下文获取实例
        TimeLine timeLine1 = (TimeLine) applicationContext.getBean("myFactoryBean");
        //MyFactoryBean
        MyFactoryBean bean = (MyFactoryBean) applicationContext.getBean("&myFactoryBean");

        List<Object> time = Lists.newArrayList();
        time.add(timeLine);
        time.add(timeLine1);
        time.add(bean);
        return time;
    }
}
