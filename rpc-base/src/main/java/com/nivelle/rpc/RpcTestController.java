package com.nivelle.rpc;
import com.nivelle.rpc.dubbo.model.DateTimeInfo;
import com.nivelle.rpc.dubbo.service.other.AsyncService;
import com.nivelle.rpc.dubbo.service.other.ConcreteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

/**
 * @author nivelle
 * @date 2019/08/17
 */
@Controller
@RequestMapping("/test")
public class RpcTestController {

    @Autowired
    AsyncService asyncService;

    /**
     * 必须添加了@Component 或则 @Service才能添加到spring容器中,但是通过 ImportBeanDefinitionRegistrar实现动态注入bean
     */
    @Autowired
    ConcreteService concreteService;


    /**
     * 异步方法调用
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("async")
    public Object testAsyncService() throws Exception {
        String result = "default";
        Future<String> asyncResult = asyncService.asyncSayHello();
        if (asyncResult.isDone()) {
            return asyncResult.get();
        }
        return result;
    }

    /**
     * 异步方法调用
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("sync")
    public Object testSyncService() throws Exception {
        String result = "default";
        Future<String> asyncResult = asyncService.asyncSayHello2();
        if (asyncResult.isDone()) {
            return asyncResult.get();
        }
        return result;
    }


    @RequestMapping(value = "/dateTime")
    @ResponseBody
    public Object dateTimeConvert() {
        DateTimeInfo dateTimeInfo = new DateTimeInfo();
        dateTimeInfo.setDateTime1(LocalDateTime.now().plusDays(1));
        dateTimeInfo.setDateTime2(LocalDateTime.now());
        dateTimeInfo.setDateTime3(LocalDateTime.now());
        return dateTimeInfo;
    }


}
