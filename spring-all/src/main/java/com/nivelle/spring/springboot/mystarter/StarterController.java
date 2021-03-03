package com.nivelle.spring.springboot.mystarter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("starter")
public class StarterController {

    //@Resource
    //private MyStarterService myStarterService;
    static List<Long> currentTransaction = new ArrayList<>(100);

    @GetMapping("/say")
    public String sayWhat() {
        //return myStarterService.say();
        return "";
    }

    /**
     * mvn install:install-file -Dfile=/Users/nivellefu/IdeaProjects/java-guides/springboot-starter/target/springboot-starter-0.0.1-SNAPSHOT.jar -DgroupId=com.nivelle.starter -DartifactId=springboot-starter -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
     */

    @RequestMapping("/statictest")
    public Object statictest(@RequestParam Long test) {
        currentTransaction.add(test);
        if (currentTransaction.size() >= 4) {
            currentTransaction.clear();
            return "执行插入";
        }
        return currentTransaction;
    }

}
