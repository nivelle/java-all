package com.nivelle.guide.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2019/07/25
 */
@Controller
@RequestMapping(value = "/kafka")
public class KafkaController {

    @Autowired
    KafkaSendService kafkaSendService;

    @RequestMapping("/send")
    public String sendKafka() {
        kafkaSendService.send();
        return "success";
    }
}
