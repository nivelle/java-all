package com.nivelle.spring;

import com.google.common.collect.Lists;
import com.nivelle.base.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JVMTest {


    @Test
    public void jConsole() {
        List<User> users = Lists.newArrayList();
        System.out.println("开始执行");
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            users.add(user);
        }
        System.out.println("执行用时");
        System.out.println(System.currentTimeMillis() - time);
    }
}
