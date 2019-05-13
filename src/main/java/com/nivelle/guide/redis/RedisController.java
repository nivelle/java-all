package com.nivelle.guide.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("redis")
public class RedisController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/string")
    @ResponseBody
    public String string() throws Exception {
        try {
            stringRedisTemplate.opsForValue().set("aaa", "111");
            String result = stringRedisTemplate.opsForValue().get("aaa");
            return result;
        }finally {
            RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
        }

    }

}
