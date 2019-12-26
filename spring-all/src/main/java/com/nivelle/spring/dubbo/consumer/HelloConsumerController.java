package com.nivelle.spring.dubbo.consumer;


import com.alibaba.dubbo.config.annotation.Reference;
import com.nivelle.base.dubboservice.HelloDubboService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * dubbo demo
 *
 * @author fuxinzhong
 * @date 2019/08/16
 */
@Controller
@RequestMapping("/dubbo")
public class HelloConsumerController {




    @Reference(version = "${helloDubbo.service.version}", check = false, cluster = "failover", retries = 2,loadbalance = "loadbalance")
    private HelloDubboService helloDubboService;


    @RequestMapping("/sayHello/{name}")
    @ResponseBody
    public String sayHello(@PathVariable String name) {
        return helloDubboService.sayHello(name);
    }


}
