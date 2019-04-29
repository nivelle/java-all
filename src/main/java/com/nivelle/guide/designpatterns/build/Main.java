package com.nivelle.guide.designpatterns.build;


/**
 * 示例化构建模式，解决构造函数太多的问题。
 */
public class Main {

    public static void main(String[] args) {

        CompanyUser companyUser = new CompanyUser.Builder("IReader").
                withAge(1).withIsMarry(false).withSalary(100).build();

        CompanyUser companyUser1 = new CompanyUser.Builder("BaiDu").
                withAge(100).withIsMarry(false).build();

        System.out.println(companyUser);
        System.out.println(companyUser1);

    }
}
