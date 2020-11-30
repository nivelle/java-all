package com.nivelle.spring.springcore.annotation.bean;

/**
 * 条件注解bean
 *
 * @author nivelle
 * @date 2020/01/16
 */
public class ConditionBean {

    private String desc;

    public ConditionBean(String desc) {
        System.err.println("通过条件注解创建了ConditionBean" + desc);
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "ConditionBean{" +
                "desc='" + desc + '\'' +
                '}';
    }
}
