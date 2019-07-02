package com.nivelle.guide.rabbitmq;

import com.nivelle.guide.springboot.configbean.RabbitMQConfig;
import com.nivelle.guide.springboot.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * rabbitMQ
 *
 * @author nivelle
 * @date 2019/07/01
 */

@Controller
@RequestMapping("rabbitMQ")
@Validated
public class RabbitMqTest {


    @Autowired
    private RabbitMQConfig rabbitMQConfig;


    @RequestMapping("/fanout")
    public Object sendFanout(@RequestBody User user) {

        rabbitMQConfig.getRabbitTemplate();


        return new Object();
    }


}
