package com.nivelle.spring.springmvc.databinder;

import com.nivelle.spring.pojo.Person;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.ServletRequestDataBinder;


/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/17
 */
public class RequestDataBinderMock {


    public static void main(String[] args) throws Exception {

        Person person = new Person();
        ServletRequestDataBinder binder = new ServletRequestDataBinder(person, "person");

        // 构造参数，此处就不用MutablePropertyValues，以HttpServletRequest的实现类MockHttpServletRequest为例吧
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 模拟请求参数
        request.addParameter("name", "fsx");
        request.addParameter("age", "18");

        // flag不仅仅可以用true/false  用0和1也是可以的？
        request.addParameter("flag", "1");

        // 设置多值的
        request.addParameter("list", new String[]{"4", "2", "3", "1"});
        // 给map赋值(Json串)
        // request.addParameter("map", "{'key1':'value1','key2':'value2'}"); // 这样可不行
        request.addParameter("map['key1']", "value1");
        request.addParameter("map['key2']", "value2");

        //一次性设置多个值（传入Map）
        //request.setParameters(new HashMap<String, Object>() {{
        //    put("name", "fsx");
        //    put("age", "18");
        //}});

        binder.bind(request);
        System.out.println(person);
    }


}
