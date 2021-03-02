package com.nivelle.spring.springboot.mystarter;

import com.nivelle.starter.MyStarterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class StarterController {

    //@Resource
    //private MyStarterService myStarterService;

    @GetMapping("/say")
    public String sayWhat() {
        //return myStarterService.say();
        return "";
    }
    /**
     * mvn install:install-file -Dfile=/Users/nivellefu/IdeaProjects/java-guides/springboot-starter/target/springboot-starter-0.0.1-SNAPSHOT.jar -DgroupId=com.nivelle.starter -DartifactId=springboot-starter -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
     */

}
