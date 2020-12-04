package com.nivelle.spring;

import com.nivelle.spring.springcore.beanFactory.Person;
import com.nivelle.spring.springcore.beanFactory.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 自动装配
 *
 * @author fuxinzhong
 * @date 2020/12/04
 */
@ContextConfiguration("classpath:autoWiringConfiguration.xml")
@RunWith(SpringRunner.class)
public class BeanFactoryTest2 {


//    @Autowired
//    private User user;

    @Autowired
    private Person person;


    @Test
    public void autoWiring() {
        System.out.println("自动装配测试");
    }
}
