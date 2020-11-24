package com.nivelle.rpc.dubbo.controller;

import com.nivelle.rpc.dubbo.model.Menu;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nivelle
 * @date 2019/07/17
 */
@Controller
@RequestMapping("test/swagger")
@Slf4j
public class SwaggerController {

    /**
     * 访问地址:http:  localhost:8080/swagger-ui.html#/swagger-controller/swaggerTestUsingGET
     * 查看生成的接口文档
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ApiOperation(
            value = "测试返回",
            notes = "测试返回列表",
            produces = "application/json, application/xml",
            consumes = "application/json, application/xml",
            response = String.class)
    @ResponseBody
    public String swaggerTest() {
        List<Menu> menus = new ArrayList<>();
        menus.add(new Menu(1L, "资源"));
        menus.add(new Menu(2L, "菜单"));
        return menus.toString();
    }
}

