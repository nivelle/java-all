package com.nivelle.programming.springboot.controllor;


import com.nivelle.programming.springboot.cache.UserFactory;
import com.nivelle.programming.springboot.pojo.User;
import com.nivelle.programming.springboot.pojo.vo.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/cache")
public class CacheController {

    @Autowired
    UserFactory userFactory;

    @RequestMapping(value = "/users")
    @ResponseBody
    public ResponseResult getUsers() {
        System.out.println("开始执行");

        List<User> userList = userFactory.users();
        System.out.println(userList);

        return ResponseResult.newResponseResult().setSuccess(userList);
    }
}
