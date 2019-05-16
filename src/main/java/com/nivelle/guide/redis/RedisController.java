package com.nivelle.guide.redis;

import com.nivelle.guide.springboot.pojo.vo.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("redis")
public class RedisController {

    @Autowired
    RedisCommandUtil redisUtil;

    @RequestMapping("/string/{key}")
    @ResponseBody
    public ResponseResult string(@PathVariable(value = "key") String key) {
        String result = redisUtil.get(key);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    @RequestMapping("/substring/{key}/{start}/{end}")
    @ResponseBody
    public ResponseResult subString(@PathVariable(value = "key") String key,
                                    @PathVariable(value = "start") Long start,
                                    @PathVariable(value = "end") Long end) {
        redisUtil.set("stringTest", "test");
        String result = redisUtil.getRange(key, start, end);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }
    /**
     * 获得旧值，设置新值
     *
     * @return
     */
    @RequestMapping("/getAndSet/{key}/{newValue}")
    @ResponseBody
    public ResponseResult getAndSetString(@PathVariable(value = "key") String key,
                                          @PathVariable(value = "newValue") String newValue) {
        String result = redisUtil.getAndSet(key, newValue);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    /**
     * 获得指定位是否有值
     *
     * @return
     */
    @RequestMapping("/getBit/{key}/{offset}")
    @ResponseBody
    public ResponseResult getBitString(@PathVariable(value = "key") String key,
                                       @PathVariable(value = "offset") long offset) {
        Boolean result = redisUtil.getBit(key, offset);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }
    /**
     * 我们在登陆某些博客网站或者视频网站的时候，网站往往会记录我们是否阅读了某篇文章，或者是观看了某个视频。
     *
     * @param key
     * @param offset
     * @param value
     * @return
     */
    @RequestMapping("/setBit/{key}/{offset}/{value}")
    @ResponseBody
    public ResponseResult setBitString(@PathVariable(value = "key") String key,
                                       @PathVariable(value = "offset") long offset,
                                       @PathVariable(value = "value") boolean value) {
        Boolean result = redisUtil.setBit(key, offset, value);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

}
