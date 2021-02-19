package com.nivelle.base.pojo;

import java.io.Serializable;

public class User implements Serializable {

    public int age;

    public String name;

    static public int flag;

    public Integer number;

    public void setAge(int age) {
        this.age = age;
    }

    public static int getFlag() {
        return flag;
    }

    public static void setFlag(int flag) {
        User.flag = flag;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "User{" +
                "age=" + age +
                ", name='" + name + '\'' +
                ", number=" + number +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        return name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String showDetail(String message) {
        return "UserDetail name is:" + this.name + "age is:" + this.age + "detail" + message;
    }

    private String show(int age, String name) {
        System.out.println(age + name);
        return "success";
    }

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

    public User(Integer age, String name) {
        this.age = age;
        this.name = name;
    }

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public User(int age, String name) {
        this.age = age;
        this.name = name;
    }
}
