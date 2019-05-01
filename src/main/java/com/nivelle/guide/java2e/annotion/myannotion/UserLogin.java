package com.nivelle.guide.java2e.annotion.myannotion;

import org.springframework.beans.factory.annotation.Autowired;

@MyAnnotation
public class UserLogin {

    @Autowired
    private IUser userdao;

    @MyAnnotation(nation = "EnglishUserImpl" )
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

