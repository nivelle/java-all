package com.nivelle.base;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivell
 * @date 2020/04/03
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {


    @RequestMapping("/sayHello")
    public String config() {

        return "hello world";
    }
}
