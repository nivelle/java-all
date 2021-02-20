package com.nivelle.ddd.infrastructure.client;

import com.nivelle.ddd.infrastructure.common.api.Response;
import ddd.leave.domain.person.entity.Person;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "auth-service", path = "/demo/auth")
public interface AuthFeignClient {

    @PostMapping(value = "/login")
    Response login(Person person);
}
