package com.nivelle.spring.rabbitmq;

import com.nivelle.spring.configbean.RabbitMQConfig;
import com.nivelle.base.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * rabbitMQ
 *
 * @author nivelle
 * @date 2019/07/01
 */

@Controller
@RequestMapping("test/rabbitMQ")
@Validated
@Slf4j
public class RabbitMqController {

    @Autowired
    private RabbitMQConfig rabbitMQConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 发往fanout 类型的 Exchange 的消息会分别转发给 Exchange绑定的队列
     *
     * @return
     */
    @RequestMapping("/fanout")
    @ResponseBody
    public Object sendFanout() {
        User user = new User();
        user.setAge(1);
        user.setName("nivelle");
        try {
            rabbitTemplate.convertAndSend(rabbitMQConfig.getFanoutExchange(),
                    rabbitMQConfig.getFanoutExchangeRoutingKey(), user.toString());
        } catch (Exception e) {
            log.error("send message error ", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 发往direct 类型的 Exchange 的消息会转发给对应的完全匹配的队列，不需要进行任何绑定操作
     * direct 类型的行为是"先匹配, 再投送". 即在绑定时设定一个 routing_key, 消息的routing_key 匹配时, 才会被交换器投送到绑定的队列中去.
     * @return
     */
    @RequestMapping("/direct1")
    @ResponseBody
    public Object sendDirect1() {
        User user = new User();
        user.setAge(2);
        user.setName("jessy");
        try {
            rabbitTemplate.convertAndSend(rabbitMQConfig.getDirectExchange(),
                    rabbitMQConfig.getDirectExchangeRoutingKey1(), user.toString());
        } catch (Exception e) {
            log.error("send message error ", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 消息只会发到key2绑定的队列
     * @return
     */
    @RequestMapping("/direct2")
    @ResponseBody
    public Object sendDirect2() {
        User user = new User();
        user.setAge(3);
        user.setName("test");
        try {
            rabbitTemplate.convertAndSend(rabbitMQConfig.getDirectExchange(),
                    rabbitMQConfig.getDirectExchangeRoutingKey2(), user.toString());
        } catch (Exception e) {
            log.error("send message error ", e.getMessage());
            return false;
        }
        return true;
    }


}
