package com.nivelle.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/03/02
 */
@ConfigurationProperties(prefix = "demo")
public class MyStarterProperties {
    private String sayWhat;
    private String toWho;

    public String getSayWhat() {
        return sayWhat;
    }

    public void setSayWhat(String sayWhat) {
        this.sayWhat = sayWhat;
    }

    public String getToWho() {
        return toWho;
    }

    public void setToWho(String toWho) {
        this.toWho = toWho;
    }
}
