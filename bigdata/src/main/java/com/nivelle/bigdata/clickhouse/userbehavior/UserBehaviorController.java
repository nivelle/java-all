package com.nivelle.bigdata.clickhouse.userbehavior;

import com.nivelle.bigdata.clickhouse.entity.UserReadBehavior;
import com.nivelle.bigdata.clickhouse.mapper.UserReadBehaviorMapper;
import org.assertj.core.util.Lists;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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

    @RequestMapping("/batchSave")
    public String saveData() {
        List<UserReadBehavior> list = Lists.newArrayList();
        for (int i = 0; i < 50; i++) {
            UserReadBehavior userReadBehavior = new UserReadBehavior();
            userReadBehavior.setUserName("SNivelle");
            userReadBehavior.setBookId(117 + i);
            userReadBehavior.setBookType(64);
            userReadBehavior.setCategoryId(2 + i);
            userReadBehavior.setCategoryName("男频");
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

    @RequestMapping("/save")
    public String save() {
        int i = 0;
        UserReadBehavior userReadBehavior = new UserReadBehavior();
        userReadBehavior.setUserName("SNivelle");
        userReadBehavior.setBookId(117 + i);
        userReadBehavior.setBookType(64);
        userReadBehavior.setCategoryId(2 + i);
        userReadBehavior.setCategoryName("男频");
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

    @RequestMapping("/selectList")
    public List<UserReadBehavior> selectList() {
        return userReadBehaviorMapper.selectList();
    }
}
