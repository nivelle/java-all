package com.nivelle.spring.springmvc.databinder;

import com.nivelle.spring.pojo.Person;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.WebDataBinder;

import java.util.HashMap;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/16
 */
public class WebDataBinderMock {

    public static void main(String[] args) {
        Person person = new Person();
        WebDataBinder binder = new WebDataBinder(person, "person");
        // 设置属性
        MutablePropertyValues pvs = new MutablePropertyValues();

        // 使用!来模拟各个字段手动指定默认值
        //pvs.add("name", "fsx");
        HashMap hashMap = new HashMap();
        hashMap.put(1,1);
        pvs.add("!name", "不知火舞");
        pvs.add("age", 18);
        pvs.add("!age", 10); // 上面有确切的值了，默认值不会再生效
        pvs.add("_list", null);
        pvs.add("_map", null);
        pvs.add("map", hashMap);
        pvs.add("_flag", null);
        pvs.add("!flag", true);
        binder.bind(pvs);
        System.out.println(person);

    }
}
