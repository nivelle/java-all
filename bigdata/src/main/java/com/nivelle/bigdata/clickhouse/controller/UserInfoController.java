package com.nivelle.bigdata.clickhouse.controller;

import com.nivelle.bigdata.clickhouse.entity.UserInfo;
import com.nivelle.bigdata.clickhouse.mapper.UserInfoMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/02/22
 */
@RestController
@RequestMapping("/user")
public class UserInfoController {
    @Resource
    private UserInfoMapper userInfoService;

    @RequestMapping("/saveData")
    public String saveData() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(4);
        userInfo.setUserName("nivelle");
        userInfo.setPassWord("567");
        userInfo.setPhone("176001400137");
        userInfo.setEmail("fix");
        userInfo.setCreateDay(LocalDate.now());
        userInfoService.saveData(userInfo);
        return "sus";
    }

    @RequestMapping("/selectById")
    public UserInfo selectById() {
        return userInfoService.selectById(1);
    }

    @RequestMapping("/selectList")
    public List<UserInfo> selectList() {
        return userInfoService.selectList();
    }


}
