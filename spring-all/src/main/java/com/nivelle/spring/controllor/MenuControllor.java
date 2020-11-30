package com.nivelle.spring.controllor;

import com.google.common.collect.Lists;
import com.nivelle.spring.pojo.Menu;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class MenuControllor {


    @GetMapping("/menus")
    public Mono<List<Menu>> hello() {

        List<Menu> menus = Lists.newArrayList();
        menus.add(new Menu(1L, "资源"));
        menus.add(new Menu(2L, "菜单"));
        return Mono.just(menus);
    }

    @GetMapping("/menus2")
    @ResponseBody
    public List<Menu> hello2() {

        List<Menu> menus = Lists.newArrayList();
        menus.add(new Menu(1L, "资源"));
        menus.add(new Menu(2L, "菜单"));
        return menus;
    }
}
