package com.nivelle.spring;


import com.nivelle.spring.pojo.UserInfoEntity;
import com.nivelle.spring.springboot.mapper.SysUserInfoMapper;
import net.minidev.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ValidTest {


    private MockMvc mockMvc; // 模拟MVC对象，通过MockMvcBuilders.webAppContextSetup(this.wac).build()初始化。


    private SysUserInfoMapper sysUserInfoMapper;

    @Autowired
    private WebApplicationContext wac; // 注入WebApplicationContext

    @Before // 在测试开始前初始化工作
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

    }


    @Test
    public void testSavePerson() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "nivelle");
        map.put("age", 0);

        MvcResult result = mockMvc.perform(post("/savePerson").content(JSONObject.toJSONString(map)))
                .andReturn();// 返回执行请求的结果

        System.out.println(result.getResponse().getContentAsString());

    }

    @Test
    public void testSysUser(){

        String userName = "admin";

        UserInfoEntity userInfoEntity = sysUserInfoMapper.getUserInfoByName(userName);

        System.out.println(userInfoEntity);

    }


}
