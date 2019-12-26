package com.nivelle.base.designpatterns.iterator;

public class Main {

    /**
     * 这种模式的优点是：iterator将遍历和实现分离开来
     *
     */

    public static void main(String[] args) {
        Company company = new Company();
        company.addNewUser(new User(1, "nivelle"));
        company.addNewUser(new User(2, "jessy"));
        company.addNewUser(new User(3, "zhage"));
        Iterator iterator = company.iterator();
        while (iterator.hashNext()) {
            User user = (User) iterator.next();
            System.out.println(user);
        }
    }
}
