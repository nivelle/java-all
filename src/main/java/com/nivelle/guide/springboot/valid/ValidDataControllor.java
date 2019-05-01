package com.nivelle.guide.springboot.valid;


import com.nivelle.guide.springboot.pojo.vo.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;


@RestController()
public class ValidDataControllor {

    private static final Logger LOG = LoggerFactory.getLogger(ValidDataControllor.class);


    @PostMapping(value = "savePerson", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseResult savePerson(@RequestBody @Validated  Person rerson) {

        LOG.info("保存用户请求参数person={}", rerson);
        Map <String,Object>result = new HashMap();//范性放在变量处
        result.put("haha","haha");

        return ResponseResult.newResponseResult().setSuccess(result);//转成json数据依赖与java对象的set方法

    }

    @GetMapping(value = "savePerson2",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseResult savePerson2(@Valid @NotNull String name){
        LOG.info("name is"+ name);
        Person person = new Person();
        person.setName(name);

        return ResponseResult.newResponseResult().setSuccess(person);
    }


}
