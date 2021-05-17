package com.nivelle.core;

import cn.hutool.http.HttpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2020/04/03
 */
@Controller
@RequestMapping("/test")
public class BaseTestController {

    @RequestMapping("/sayHello")
    public String config() {
        return "hello world";
    }

    /**
     * httpUtil
     *
     * @return
     */
    @RequestMapping("httpClient")
    public String httpClient() {
        String data = HttpUtil.get("http://localhost:8080/springAll/test/writeData");
        System.out.println(data);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println();
        }
        return "";
    }


}
