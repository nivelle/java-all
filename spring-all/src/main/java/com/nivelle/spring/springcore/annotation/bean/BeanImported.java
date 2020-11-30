package com.nivelle.spring.springcore.annotation.bean;

import lombok.Data;

/**
 * 通过 ImportBeanDefinitionRegistrar 将 Definition 注入容器
 *
 * @author nivelle
 * @date 2020/01/17
 */
@Data
public class BeanImported {

    private String userName;

    @Override
    public String toString() {
        return "BeanImported{" +
                "userName='" + userName + '\'' +
                '}';
    }
}
