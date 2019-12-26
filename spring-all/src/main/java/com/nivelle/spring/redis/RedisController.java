package com.nivelle.spring.redis;

import com.nivelle.base.pojo.vo.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("test/redis")
public class RedisController {

    @Autowired
    RedisCommandService redisCommandService;

    /*****************************************key操作********************************************************/

    /**
     * 删除键
     *
     * @return ResponseResult
     */
    @RequestMapping("/deleteKey/{key}")
    @ResponseBody
    public ResponseResult deleteKey(@PathVariable(value = "key") String key) {
        redisCommandService.delete(key);
        return ResponseResult.newResponseResult().setSuccess("delete key=" + key + " is success");
    }


    /**
     * 查看所有的键
     *
     * @return ResponseResult
     */
    @RequestMapping("/keys/{pattern}")
    @ResponseBody
    public ResponseResult Key(@PathVariable(value = "pattern") String pattern) {
        Set<String> set = redisCommandService.keys(pattern);
        return ResponseResult.newResponseResult().setSuccess(set);
    }

    /**
     * 设置键的过期时间
     *
     * @return ResponseResult
     */
    @RequestMapping("/expireTime/{key}/{timeout}")
    @ResponseBody
    public ResponseResult expireKey(@PathVariable(value = "key") String key, @PathVariable(value = "timeout") Long timeout) {
        boolean expire = redisCommandService.expire(key, timeout, TimeUnit.SECONDS);
        return ResponseResult.newResponseResult().setSuccess(expire);
    }

    /**
     * 查看键的过期时间
     *
     * @return ResponseResult
     */
    @RequestMapping("/expire/{key}")
    @ResponseBody
    public ResponseResult getExpire(@PathVariable(value = "key") String key) {
        long expire = redisCommandService.getExpire(key);
        return ResponseResult.newResponseResult().setSuccess(expire);
    }


    /*****************************************zSet操作********************************************************/

    /**
     * 获取键对应的值
     *
     * @param key
     * @return ResponseResult
     */
    @RequestMapping("/string/{key}")
    @ResponseBody
    public ResponseResult string(@PathVariable(value = "key") String key) {
        String result = redisCommandService.get(key);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    /**
     * 键是否存在
     *
     * @param key
     * @return ResponseResult
     */
    @RequestMapping("/substring/{key}")
    @ResponseBody
    public ResponseResult exists(@PathVariable(value = "key") String key) {
        redisCommandService.set("stringTest", "test");
        boolean result = redisCommandService.hasKey(key);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

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
        redisCommandService.set("stringTest", "test");
        String result = redisCommandService.getRange(key, start, end);
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
        redisCommandService.setEx(key, value, timeout, TimeUnit.SECONDS);
        return ResponseResult.newResponseResult().setSuccess("success");
    }

    /*****************************************位图操作********************************************************/

    /**
     * 获得指定位是否有值
     *
     * @return
     */
    @RequestMapping("/getBit/{key}/{offset}")
    @ResponseBody
    public ResponseResult getBitString(@PathVariable(value = "key") String key,
                                       @PathVariable(value = "offset") long offset) {
        Boolean result = redisCommandService.getBit(key, offset);
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
        Boolean result = redisCommandService.setBit(key, offset, value);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }


    /*****************************************zSet操作********************************************************/

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
        boolean result = redisCommandService.zAdd(key, value, score);
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
        long scores = redisCommandService.zRank(key, value);
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
        long scores = redisCommandService.zReverseRank(key, value);
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
        Set<String> set = redisCommandService.zRange(key, start, end);
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
        Set<ZSetOperations.TypedTuple<String>> set = redisCommandService.zRangeWithScores(key, start, end);
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
        Set<String> set = redisCommandService.zRangeByScore(key, min, max);
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
        Set<ZSetOperations.TypedTuple<String>> set = redisCommandService.zRangeByScoreWithScores(key, min, max);
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
        long zSize = redisCommandService.zSize(key);
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
        long zZCard = redisCommandService.zZCard(key);
        return ResponseResult.newResponseResult().setSuccess(zZCard);
    }

    /*****************************************Set操作********************************************************/

    /**
     * 批量添加
     *
     * @param key
     * @return
     */
    @RequestMapping("/sAdd/{key}/{value1}/{value2}/{value3}")
    @ResponseBody
    public ResponseResult sAdd(@PathVariable(value = "key") String key,
                               @PathVariable(value = "value1") String value1,
                               @PathVariable(value = "value2") String value2,
                               @PathVariable(value = "value3") String value3) {
        long size = redisCommandService.sAdd(key, value1, value2, value3);
        return ResponseResult.newResponseResult().setSuccess(size);
    }

    /**
     * 返回集合的元素
     *
     * @param key
     * @return
     */
    @RequestMapping("/setMembers/{key}")
    @ResponseBody
    public ResponseResult setMembers(@PathVariable(value = "key") String key) {
        Set<String> set = redisCommandService.setMembers(key);
        return ResponseResult.newResponseResult().setSuccess(set);
    }

    /**
     * 移除集合的元素
     *
     * @param key
     * @param values
     * @return
     */
    @RequestMapping("/sRemove/{key}/{values}")
    @ResponseBody
    public ResponseResult sRemove(@PathVariable(value = "key") String key, @PathVariable(value = "values") String values) {
        Long result = redisCommandService.sRemove(key, values.split(","));
        return ResponseResult.newResponseResult().setSuccess(result);
    }


    /**
     * 随机移除并返回集合中的一个元素（抽奖）
     *
     * @param key
     * @return ResponseResult
     */
    @RequestMapping("/sPop/{key}")
    @ResponseBody
    public ResponseResult sRemove(@PathVariable(value = "key") String key) {
        String result = redisCommandService.sPop(key);
        return ResponseResult.newResponseResult().setSuccess(result);
    }


    /**
     * 返回集合的大小
     *
     * @param key
     * @return
     */
    @RequestMapping("/setSize/{key}")
    @ResponseBody
    public ResponseResult setSize(@PathVariable(value = "key") String key) {
        Long result = redisCommandService.sSize(key);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    /*****************************************hash操作********************************************************/

    /**
     * 添加元素
     *
     * @param key
     * @return
     */
    @RequestMapping("/addHash/{key}/{hKey}/{hValue}")
    @ResponseBody
    public ResponseResult hPut(@PathVariable(value = "key") String key,
                               @PathVariable(value = "hKey") String hKey,
                               @PathVariable(value = "hValue") String hValue) {
        redisCommandService.hPut(key, hKey, hValue);
        return ResponseResult.newResponseResult().setSuccess("");
    }

    /**
     * 返回hash集合中指定键所有元素
     *
     * @param key
     * @return
     */
    @RequestMapping("/hGetAll/{key}")
    @ResponseBody
    public ResponseResult hGetAll(@PathVariable(value = "key") String key) {
        Map<Object, Object> result = redisCommandService.hGetAll(key);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    /*****************************************List操作********************************************************/

    /**
     * list添加操作
     *
     * @param key
     * @return
     */
    @RequestMapping("/listLPush/{key}/{value}")
    @ResponseBody
    public ResponseResult lLeftPush(@PathVariable(value = "key") String key,@PathVariable(value = "value") String value) {
        Long result = redisCommandService.lLeftPush(key,value);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    /**
     * list添加操作
     *
     * @param key
     * @return
     */
    @RequestMapping("/lIndex/{key}/{index}")
    @ResponseBody
    public ResponseResult lIndex(@PathVariable(value = "key") String key,@PathVariable(value = "index") long index) {
        String result = redisCommandService.lIndex(key,index);
        return ResponseResult.newResponseResult().setSuccess(result);
    }


}
