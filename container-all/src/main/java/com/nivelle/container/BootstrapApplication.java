package com.nivelle.container;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * war包方式启动
 *
 * @author fuxinzhong
 * @date 2020/01/06
 */
@SpringBootApplication
public class BootstrapApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BootstrapApplication.class);
    }


}
