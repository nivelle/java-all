package com.nivelle.spring.springmvc.databinder;

import com.ctc.wstx.shaded.msv_core.grammar.DataExp;
import com.nivelle.spring.pojo.Person;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

import java.util.Date;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/18
 */
public class MyDateEditorMock {

    public static void main(String[] args) {
        Person person = new Person();
        DataBinder binder = new DataBinder(person, "person");
        binder.registerCustomEditor(Date.class, new MyDatePropertyEditor());
        //binder.registerCustomEditor(Date.class, "end", new MyDatePropertyEditor());

        // 设置属性
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.add("name", "fsx");

        // 事件类型绑定
        pvs.add("start", new Date());
        pvs.add("end", "2019-07-20");
        // 试用试用标准的事件日期字符串形式~
        pvs.add("endTest", "Sat Jul 20 11:00:22 CST 2019");

        binder.bind(pvs);
        System.out.println(person);
    }

}
