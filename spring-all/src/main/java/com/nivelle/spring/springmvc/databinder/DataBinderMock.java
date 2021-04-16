package com.nivelle.spring.springmvc.databinder;

import com.nivelle.spring.pojo.Person;
import com.nivelle.spring.springmvc.PersonValidator;
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
        PersonValidator personValidator = new PersonValidator();
//        binder.setAllowedFields("age");
//        binder.setAllowedFields("name");
        pvs.add("!name", "不知火舞");
        pvs.add("age", 18);
        // 上面有确切的值了，默认值不会再生效
        pvs.add("!age", 10);
        binder.setValidator(personValidator);
        binder.bind(pvs);
        binder.validate();
        System.out.println(person);
        BindingResult bindingResult = binder.getBindingResult();
        System.out.println(bindingResult);
    }
}
