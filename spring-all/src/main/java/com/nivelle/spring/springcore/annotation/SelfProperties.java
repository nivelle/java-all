package com.nivelle.spring.springcore.annotation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.properties 配置默认的key实现原理
 *
 * @author fuxinzhong
 * @date 2020/01/16
 */
@ConfigurationProperties(prefix = "myConfig")
public class SelfProperties {

    /**
     * 要用全key名字
     */
    @Value("${myConfig.desc}")
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void printDesc() {
        System.out.println(this.desc);
    }
}
