package com.nivelle.guide.designpatterns.build;

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
