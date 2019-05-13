package com.nivelle.guide.redis;

import com.nivelle.guide.springboot.pojo.vo.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("redis")
public class RedisController {

    @Autowired
    RedisUtil redisUtil;

    @RequestMapping("/string")
    @ResponseBody
    public ResponseResult string()  {
        //redisUtil.set("stringTest", "test");
        String result = redisUtil.get("stringTest");
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    @RequestMapping("/substring")
    @ResponseBody
    public ResponseResult subString() {
        redisUtil.set("stringTest", "test");
        String result = redisUtil.getRange("stringTest",1L,(long)"stringTest".length());
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }


    @RequestMapping("/getAndSet")
    @ResponseBody
    public ResponseResult getAndSetString() {
        String result = redisUtil.getAndSet("stringTest","nivelleTest");
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    @RequestMapping("/getBit")
    @ResponseBody
    public ResponseResult getBitString(@RequestParam Long offect) {
        Boolean result = redisUtil.getBit("stringTest",offect);
        System.out.println(result);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

}
