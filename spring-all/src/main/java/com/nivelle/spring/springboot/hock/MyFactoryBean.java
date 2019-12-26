package com.nivelle.spring.springboot.hock;

import com.nivelle.base.pojo.TimeLine;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * factoryBean 工厂方法
 */
@Component
public class MyFactoryBean implements FactoryBean<TimeLine> {

    @Override
    public TimeLine getObject() throws Exception {
        TimeLine timeLine = new TimeLine();
        timeLine.setContent("小溪");
        timeLine.setDateTime(new Date());
        timeLine.setName("nivelle");
        return timeLine;
    }

    @Override
    public Class<?> getObjectType() {
        return TimeLine.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
