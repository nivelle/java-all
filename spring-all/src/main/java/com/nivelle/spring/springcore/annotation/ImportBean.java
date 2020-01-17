package com.nivelle.spring.springcore.annotation;

/**
 * 通过 ImportBeanDefinitionRegistrar 将 Definition 注入容器
 *
 * @author fuxinzhong
 * @date 2020/01/17
 */
public class ImportBean {

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String userName;


}
