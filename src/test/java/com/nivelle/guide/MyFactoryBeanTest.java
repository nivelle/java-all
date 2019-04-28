package com.nivelle.guide;


import com.nivelle.guide.springboot.hock.MyFactoryBean;
import com.nivelle.guide.springboot.pojo.TimeLine;
import com.nivelle.guide.springboot.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MyFactoryBeanTest{

    @Autowired
    private MyFactoryBean myFactoryBean;

    @Autowired
    private ApplicationContext applicationContext;


    @Test
    public void getObject() throws Exception {
        //直接通过#getObject获取实例
        TimeLine timeLine = myFactoryBean.getObject();
        System.out.println(timeLine.toString());
        //通过Spring上下文获取实例
        User timeLine1 = (User) applicationContext.getBean("user");
        System.out.println(timeLine1);
        //MyFactoryBean
        MyFactoryBean bean = (MyFactoryBean) applicationContext.getBean("&myFactoryBean");
        System.out.println(bean);
    }
}
