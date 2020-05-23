package com.nivelle.rpc.dubbo.controller;

import com.nivelle.rpc.dubbo.service.dubboservice.HelloDubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/05/10
 */
@Controller
@RequestMapping("/doubo")
public class DubboController {

    @Autowired
    HelloDubboService helloDubboService;

    @RequestMapping(value = "/hello")
    @ResponseBody
    public Object helloDubbo() {
        String result = helloDubboService.sayHello("ee");
        return result;
    }
}
