package com.nivelle.bigdata.clickhouse.controller;

import com.google.common.collect.Maps;
import com.nivelle.bigdata.clickhouse.entity.UserReadBehavior;
import com.nivelle.bigdata.clickhouse.mapper.UserReadBehaviorMapper;
import com.nivelle.bigdata.clickhouse.params.UserReadBehaviorResponse;
import org.assertj.core.util.Lists;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

/**
 * @author fuxinzhong
 * @date 2021/02/22
 */
@RestController
@RequestMapping("/behavior")
public class UserBehaviorController {
    @Resource
    private UserReadBehaviorMapper userReadBehaviorMapper;

    /**
     * 批量添加
     *
     * @return
     */
    @RequestMapping("/batchSave")
    public String saveData() {
        List<UserReadBehavior> list = Lists.newArrayList();
        for (int i = 0; i < 50; i++) {
            UserReadBehavior userReadBehavior = new UserReadBehavior();
            userReadBehavior.setUserName("SNivelle");
            userReadBehavior.setBookId(117 + i);
            userReadBehavior.setBookType(64);
            userReadBehavior.setCategoryId1(2 + i);
            userReadBehavior.setCategoryId2(3+i);
            userReadBehavior.setCategoryId3(3+i);
            userReadBehavior.setCategoryId4(3+i);
            userReadBehavior.setChapterId("201-1" + "-" + i);
            userReadBehavior.setCopyrightId(125 + i);
            userReadBehavior.setInCoPkg(1);
            userReadBehavior.setIp("10.100.96.121");
            userReadBehavior.setP16("ios");
            userReadBehavior.setCompanyId("200251");
            userReadBehavior.setReadTimes(i);
            userReadBehavior.setUserGroupId(i);
            userReadBehavior.setCreateTime(LocalDateTime.now());
            list.add(userReadBehavior);
        }
        userReadBehaviorMapper.batchSave(list);
        return "sus";
    }

    /**
     * 单个添加
     *
     * @return
     */
    @RequestMapping("/save")
    public String save() {
        int i = 0;
        UserReadBehavior userReadBehavior = new UserReadBehavior();
        userReadBehavior.setUserName("jessy");
        userReadBehavior.setBookId(117 + i);
        userReadBehavior.setBookType(64);
        userReadBehavior.setCategoryId1(2 + i);
        userReadBehavior.setCategoryId2(3+i);
        userReadBehavior.setCategoryId3(3+i);
        userReadBehavior.setCategoryId4(3+i);
        userReadBehavior.setChapterId("201-1" + "-" + i);
        userReadBehavior.setCopyrightId(125 + i);
        userReadBehavior.setInCoPkg(1);
        userReadBehavior.setIp("10.100.96.121");
        userReadBehavior.setP16("ios");
        userReadBehavior.setCompanyId("200251");
        userReadBehavior.setReadTimes(i);
        userReadBehavior.setUserGroupId(i);
        userReadBehavior.setCreateTime(LocalDateTime.now());

        userReadBehaviorMapper.save(userReadBehavior);
        return "sus";
    }

    /**
     * 批量查询
     *
     * @return
     */
    @RequestMapping("/selectList")
    public List<UserReadBehavior> selectList() {
        return userReadBehaviorMapper.selectList();
    }

    @RequestMapping("/getByCondition")
    public Object getListByCondition() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime startTime = LocalDateTime.parse("2021-02-25 00:00:00", dtf);
        LocalDateTime endTime = LocalDateTime.parse("2021-02-25 23:00:00", dtf);
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("bookIds", Lists.newArrayList(117, 166));
        List<UserReadBehaviorResponse> result = userReadBehaviorMapper.getCondition(params);
        System.out.println(result);
        return result;
    }
}
