package com.nivelle.spring.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2019/07/25
 */
@Controller
@RequestMapping(value = "test/kafka")
public class KafkaController {

    @Autowired
    KafkaSendService kafkaSendService;

    /**
     * 自定义分区
     *
     * @return
     */
    @RequestMapping("/send1")
    @ResponseBody
    public String sendKafka() {
        kafkaSendService.send1();
        return "success";
    }

    /**
     * 默认分区
     *
     * @return
     */
    @RequestMapping("/send2")
    @ResponseBody
    public String sendKafka2() {
        kafkaSendService.send2();
        return "success";
    }

    /**
     * 过滤器
     *
     * @return
     */
    @RequestMapping("/send3")
    @ResponseBody
    public String sendKafka3() {
        kafkaSendService.send3();
        return "success";
    }

    /**
     * 过滤器
     *
     * @return
     */
    @RequestMapping("/send4")
    @ResponseBody
    public String sendKafka4() {
        kafkaSendService.send4();
        return "success";
    }

    /**
     * 批处理
     *
     * @return
     */
    @RequestMapping("/send5")
    @ResponseBody
    public String sendKafka5() {
        kafkaSendService.send5();
        return "success";
    }

    /**
     * 消息头
     *
     * @return
     */
    @RequestMapping("/send6")
    @ResponseBody
    public String sendKafka6() {
        kafkaSendService.send6();
        return "success";
    }

    /**
     * 监听Topic中指定的分区
     *
     * @return
     */
    @RequestMapping("/send7")
    @ResponseBody
    public String sendKafka7() {
        try {
            kafkaSendService.send7();
        } catch (Exception e) {
            System.err.println(e);
        }
        return "success";
    }
}
