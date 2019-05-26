package com.nivelle.guide.redis;

import com.nivelle.guide.springboot.pojo.vo.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("redis")
public class RedisController {

    @Autowired
    RedisCommandUtil redisCommandUtil;

    /**
     * 获取值的子集
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @RequestMapping("/substring/{key}/{start}/{end}")
    @ResponseBody
    public ResponseResult subString(@PathVariable(value = "key") String key,
                                    @PathVariable(value = "start") Long start,
                                    @PathVariable(value = "end") Long end) {
        redisCommandUtil.set("stringTest", "test");
        String result = redisCommandUtil.getRange(key, start, end);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    /**
     * 获取键对应的值
     *
     * @param key
     * @return
     */
    @RequestMapping("/string/{key}")
    @ResponseBody
    public ResponseResult string(@PathVariable(value = "key") String key) {
        String result = redisCommandUtil.get(key);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    /**
     * 键是否存在
     *
     * @param key
     * @return
     */
    @RequestMapping("/substring/{key}")
    @ResponseBody
    public ResponseResult exists(@PathVariable(value = "key") String key) {
        redisCommandUtil.set("stringTest", "test");
        boolean result = redisCommandUtil.hasKey(key);
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
        String result = redisCommandUtil.getAndSet(key, newValue);
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
        Boolean result = redisCommandUtil.getBit(key, offset);
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
        Boolean result = redisCommandUtil.setBit(key, offset, value);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }


    /**
     * 设置key:value 并设置过期时间
     *
     * @param key
     * @param value
     * @return
     */
    @RequestMapping("/setEx/{key}/{value}/{timeout}")
    @ResponseBody
    public ResponseResult setEx(@PathVariable(value = "key") String key,
                                @PathVariable(value = "value") String value,
                                @PathVariable(value = "timeout") long timeout) {
        redisCommandUtil.setEx(key, value, timeout, TimeUnit.SECONDS);
        return ResponseResult.newResponseResult().setSuccess("success");
    }

    //zSet操作

    /**
     * 设置 zSet 有序集合并设置分数
     *
     * @param key
     * @param value
     * @return
     */
    @RequestMapping("/zAdd/{key}/{value}/{score}")
    @ResponseBody
    public ResponseResult zAdd(@PathVariable(value = "key") String key,
                               @PathVariable(value = "value") String value,
                               @PathVariable(value = "score") double score) {
        boolean result = redisCommandUtil.zAdd(key, value, score);
        return ResponseResult.newResponseResult().setSuccess(result);
    }


    /**
     * 设置 zSet 返回指定元素的排名从小到大
     *
     * @param key
     * @param value
     * @return
     */
    @RequestMapping("/zRank/{key}/{value}")
    @ResponseBody
    public ResponseResult zRank(@PathVariable(value = "key") String key,
                                @PathVariable(value = "value") String value) {
        long scores = redisCommandUtil.zRank(key, value);
        return ResponseResult.newResponseResult().setSuccess(scores);
    }

    /**
     * 设置 zSet 返回指定元素的排名从大到小
     *
     * @param key
     * @param value
     * @return
     */
    @RequestMapping("/zReverseRank/{key}/{value}")
    @ResponseBody
    public ResponseResult zReverseRank(@PathVariable(value = "key") String key,
                                       @PathVariable(value = "value") String value) {
        long scores = redisCommandUtil.zReverseRank(key, value);
        return ResponseResult.newResponseResult().setSuccess(scores);
    }


    /**
     * 设置 zSet 返回指定元素的排名从小到大
     *
     * @param key
     * @param start
     * @param end   -1 到末尾
     * @return
     */
    @RequestMapping("/zRange/{key}/{start}/{end}")
    @ResponseBody
    public ResponseResult zRange(@PathVariable(value = "key") String key,
                                 @PathVariable(value = "start") long start,
                                 @PathVariable(value = "end") long end) {
        Set<String> set = redisCommandUtil.zRange(key, start, end);
        return ResponseResult.newResponseResult().setSuccess(set);
    }


    /**
     * 设置 zSet 返回指定元素的排名带着分数从小到大
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @RequestMapping("/zRangeWithScores/{key}/{start}/{end}")
    @ResponseBody
    public ResponseResult zRangeWithScores(@PathVariable(value = "key") String key,
                                           @PathVariable(value = "start") long start,
                                           @PathVariable(value = "end") long end) {
        Set<ZSetOperations.TypedTuple<String>> set = redisCommandUtil.zRangeWithScores(key, start, end);
        return ResponseResult.newResponseResult().setSuccess(set);
    }


    /**
     * 设置 zSet 返回指定元素的排名带着分数从小到大
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    @RequestMapping("/zRangeByScore/{key}/{min}/{max}")
    @ResponseBody
    public ResponseResult zRangeByScore(@PathVariable(value = "key") String key,
                                        @PathVariable(value = "min") double min,
                                        @PathVariable(value = "max") double max) {
        Set<String> set = redisCommandUtil.zRangeByScore(key, min, max);
        return ResponseResult.newResponseResult().setSuccess(set);
    }

    /**
     * 设置 zSet 返回指定元素的排名带着分数从小到大,带着分数
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    @RequestMapping("/zRangeByScoreWithScores/{key}/{min}/{max}")
    @ResponseBody
    public ResponseResult zRangeByScoreWithScores(@PathVariable(value = "key") String key,
                                                  @PathVariable(value = "min") double min,
                                                  @PathVariable(value = "max") double max) {
        Set<ZSetOperations.TypedTuple<String>> set = redisCommandUtil.zRangeByScoreWithScores(key, min, max);
        return ResponseResult.newResponseResult().setSuccess(set);
    }

    /**
     * 返回集合的大小
     *
     * @param key
     * @return
     */
    @RequestMapping("/zSize/{key}")
    @ResponseBody
    public ResponseResult zSize(@PathVariable(value = "key") String key) {
        long zSize = redisCommandUtil.zSize(key);
        return ResponseResult.newResponseResult().setSuccess(zSize);
    }

    /**
     * 返回集合的大小
     *
     * @param key
     * @return
     */
    @RequestMapping("/zZCard/{key}")
    @ResponseBody
    public ResponseResult zZCard(@PathVariable(value = "key") String key) {
        long zZCard = redisCommandUtil.zZCard(key);
        return ResponseResult.newResponseResult().setSuccess(zZCard);
    }

}
