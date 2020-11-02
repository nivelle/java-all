package com.nivelle.base.jdk.annotion.myannotion;

import org.springframework.beans.factory.annotation.Autowired;

@MyAnnotation(name = "myAnnotationTest")
public class UserLogin {

    @Autowired
    private IUser userService;

    public void setUserService(IUser userService) {
        this.userService = userService;
    }

    @MyAnnotation(nation = "ChineseUserImpl")
    public void loginTest() {
        userService.login();
    }
}

