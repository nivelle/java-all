package com.nivelle.guide.spring.initdemo;


import com.nivelle.guide.springboot.entity.ActivityPvEntity;
import com.nivelle.guide.springboot.service.ActivityService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Component
public class InitSpringBean implements InitializingBean, ApplicationListener<ContextRefreshedEvent> {

    @Resource
    ActivityService activityService;

    public InitSpringBean() {
        System.err.println("----> InitSequenceBean: constructor: " + activityService);
    }

    @PostConstruct
    public void postConstruct() {
        System.err.println("----> InitSequenceBean: postConstruct: " + activityService);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.err.println("----> InitSequenceBean: afterPropertiesSet: " + activityService);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent arg0) {
        System.err.println("----> InitSequenceBean: onApplicationEvent");
    }

    public List<ActivityPvEntity> getActivities() {
        List<ActivityPvEntity> list = activityService.getAll();
        return list;
    }
}
