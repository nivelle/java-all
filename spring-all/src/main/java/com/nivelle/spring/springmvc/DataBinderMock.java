package com.nivelle.spring.springmvc;

import com.nivelle.spring.pojo.Person;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

/**
 * spring 数据绑定
 *
 * @author fuxinzhong
 * @date 2021/04/15
 */
public class DataBinderMock {

    public static void main(String[] args) throws Exception {
        Person person = new Person();
        DataBinder binder = new DataBinder(person, "person");
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("name", "nivelle");
        pvs.add("age", 120);
        binder.setAllowedFields("age");
        PersonValidator personValidator = new PersonValidator();
        binder.setValidator(personValidator);
        binder.bind(pvs);
        binder.validate();

        System.out.println(person.getName());
        System.out.println(person.getAge());
        BindingResult bindingResult = binder.getBindingResult();
        System.out.println(bindingResult);
    }
}
