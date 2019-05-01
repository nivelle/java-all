package com.nivelle.guide.springboot.cache;

import com.google.common.collect.Lists;
import com.nivelle.guide.springboot.pojo.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "users")
public class UserFactory {

    @Cacheable
    public List<User> users() {
        System.out.println("查询数据库");
        List users = Lists.newArrayList();
        users.add(new User(1, "nivelle"));
        users.add(new User(2, "jessy"));

        return users;

    }
}
