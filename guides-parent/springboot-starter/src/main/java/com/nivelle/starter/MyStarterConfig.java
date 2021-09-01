package com.nivelle.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/03/02
 */
@Configuration
@EnableConfigurationProperties(MyStarterProperties.class)
@ConditionalOnProperty(prefix = "demo", name = "isOpen", havingValue = "true")
public class MyStarterConfig {


    private MyStarterProperties myStarterProperties;

    public MyStarterService myStarterProperties() {
        return new MyStarterService(myStarterProperties.getSayWhat(), myStarterProperties.getToWho());
    }
}
