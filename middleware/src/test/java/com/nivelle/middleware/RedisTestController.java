package com.nivelle.middleware;

import com.nivelle.middleware.redis.RedisServiceApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 测试
 *
 * @author fuxinzhong
 * @date 2020/10/20
 */
@SpringBootTest
public class RedisTestController {
    @Autowired
    RedisServiceApi redisCommandService;

    @Test
    public void assString() throws Exception {
        redisCommandService.set("name", "nivelle");
        String value = redisCommandService.get("name");
        Assert.assertEquals("nivelle", value);
    }
}
