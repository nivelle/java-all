package com.nivelle.rpc;

import com.nivelle.rpc.model.Cat;
import com.nivelle.rpc.model.Dog;
import com.nivelle.rpc.service.ConcreteService;
import com.nivelle.rpc.model.DateTimeInfo;
import com.nivelle.rpc.service.other.AsyncService;
import com.nivelle.rpc.service.other.XmlBeanServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

/**
 * @author fuxinzhong
 * @date 2019/08/17
 */
@Controller
@RequestMapping("/test")
public class RpcTestController {
    /**
     * 获取上下文
     */
    @Autowired
    WebApplicationContext webApplicationConnect;

    @Autowired
    AsyncService asyncService;


    /**
     * 必须添加了@Component 或则 @Service才能添加到spring容器中,但是通过 ImportBeanDefinitionRegistrar实现动态注入bean
     */
    @Autowired
    ConcreteService concreteService;

    @RequestMapping("/ok")
    public Object test() {
        System.out.println("dubbo provider is ok");
        /**
         * springboot默认属性未设置值时为null,可设置为""
         */
        //UserInfo userInfo = new UserInfo();
        Object object = webApplicationConnect.getBean("userInfo");
        return object;
    }

    /**
     * 通过xml配置文件导入不能自动扫描到的实例
     *
     * @return
     */
    @RequestMapping("xml")
    public Object testXmlService() {
        Object xmlBeanService = webApplicationConnect.getBean("xmlService");
        XmlBeanServiceImpl xmlBeanService1 = (XmlBeanServiceImpl) xmlBeanService;
        return xmlBeanService1.helloXmlService();
    }

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

    /**
     * 多实例测试
     *
     * @return
     */
    @RequestMapping("/prototypeBean")
    public Object prototypeBeanTest() {
        Dog dog = (Dog) webApplicationConnect.getBean("buDingDog");
        Dog dog2 = (Dog) webApplicationConnect.getBean("buDingDog");
        if (dog == dog2) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 单实例测试
     *
     * @return
     */
    @RequestMapping("/singletonBean")
    public Object singletonTest() {
        Dog dog = (Dog) webApplicationConnect.getBean("bigDog");
        System.out.println(dog.getClass().getName());
        Dog dog2 = (Dog) webApplicationConnect.getBean("bigDog");
        if (dog == dog2) {
            return true;
        } else {
            return false;
        }
    }

    @RequestMapping("/registerBean")
    public Object registerBean() {
        Cat cat = (Cat) webApplicationConnect.getBean("com.nivelle.guide.model.Cat");
        System.out.println(cat);
        return cat;
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
