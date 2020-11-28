package com.nivelle.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * @Author nivelle
 */
@SpringBootApplication
@Import({TaskPoolConfig.class})
public class JavaBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaBaseApplication.class, args);
    }

}
