package com.nivelle.container;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 容器学习
 *
 * @author nivell
 * @date 2020/04/03
 */
@RestController
@RequestMapping(value = "/test")
public class ContainerController {


    @RequestMapping("/sayHello")
    public String config() {

        return "hello world";
    }
}
