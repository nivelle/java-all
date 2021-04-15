package com.nivelle.spring.springcore;

import com.nivelle.spring.pojo.Person;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

import java.util.Map;

/**
 * spring 数据绑定
 *
 * @author fuxinzhong
 * @date 2021/04/15
 */
public class DataBinderMock {

    public static void main(String[] args) throws Exception{
        Person person = new Person();
        DataBinder binder = new DataBinder(person, "person");
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("name", "nivelle");
        pvs.add("age", 18);
        binder.bind(pvs);
        Map<?, ?> close = binder.close();
        System.out.println(person.getName());
    }
}
