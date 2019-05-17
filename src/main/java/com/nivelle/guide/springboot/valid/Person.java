package com.nivelle.guide.springboot.valid;


import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;



public class Person {

    @NotNull(message = "年龄不能为空")//测试发现是说key不能为空注意哦
    @Min(value = 0, message = "年龄不能小于0岁")
    private Integer age;

    @NotNull(message = "姓名不能为空")
    private String name;

    private String addRess;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddRess() {
        return addRess;
    }

    public void setAddRess(String addRess) {
        this.addRess = addRess;
    }
}
