package com.nivelle.ddd.application.service;

import com.nivelle.ddd.domain.person.entity.Person;
import com.nivelle.ddd.domain.person.service.PersonDomainService;
import com.nivelle.ddd.infrastructure.client.AuthFeignClient;
import com.nivelle.ddd.infrastructure.common.api.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginApplicationService{

    @Autowired
    AuthFeignClient authService;

    @Autowired
    PersonDomainService personDomainService;


    public Response login(Person person){
        //调用鉴权微服务
        return authService.login(person);
    }
}