package com.nivelle.programming.java2e.annotion.myannotion;

import com.nivelle.programming.java2e.annotion.myannotion.IUser;
import com.nivelle.programming.java2e.annotion.myannotion.MyAnnotation;
import org.springframework.beans.factory.annotation.Autowired;

@MyAnnotation
public class UserLogin {

    @Autowired
    private IUser userdao;

    @MyAnnotation(nation = "EnglishUserImpl")
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

