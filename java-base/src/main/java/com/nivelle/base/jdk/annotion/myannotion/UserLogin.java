package com.nivelle.base.jdk.annotion.myannotion;

import org.springframework.beans.factory.annotation.Autowired;

@MyAnnotation(name = "myAnnotationTest")
public class UserLogin {

    @Autowired
    @MyAnnotation(nation = "test")
    private IUser userdao;

    public void setUserdao(IUser userdao) {
        this.userdao = userdao;
    }
    public IUser getUserdao() {
        return userdao;
    }

    public void loginTest() {
        userdao.login();
    }
}

