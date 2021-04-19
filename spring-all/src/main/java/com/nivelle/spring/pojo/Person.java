package com.nivelle.spring.pojo;


import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class Person {

    //@NotNull(message = "年龄不能为空")//测试发现是说key不能为空注意哦
    //@Min(value = 0, message = "年龄不能小于0岁")
    public int age;

    //@NotNull(message = "姓名不能为空")
    public String name;

    public String address;

    public Boolean flag;
    public int index;
    public List<String> list;
    public Map<String, String> map;

    private Date start;
    private Date end;
    private Date endTest;


}
