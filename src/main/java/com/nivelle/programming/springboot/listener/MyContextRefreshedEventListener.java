package com.nivelle.programming.springboot.listener;

import com.nivelle.programming.springboot.pojo.TimeLine;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 上下文已经准备完毕的时候触发
 * <p>
 * 用于容器初始化完成之后，执行需要处理的一些操作，比如一些数据的加载、初始化缓存、特定任务的注册等等
 */
@Component

public class MyContextRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * 需要注意的是，在普通Spring环境中，基于ApplicationListener的监听器的onApplicationEvent方法可能会被执行多次
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("MyContextRefreshedEventListener 监听到容器初始化成功！也可以注册bean");
        if (event.getApplicationContext().getParent() == null) {
            //ignore
        }

        ApplicationContext applicationContext = event.getApplicationContext();
        TimeLine bean = applicationContext.getAutowireCapableBeanFactory().createBean(TimeLine.class);
        bean.setName("jessy");
        bean.setContent("fuck");
        bean.setDateTime(new Date());

        System.out.println("自己创建出来的bean" + bean.getClass().getSimpleName());


    }
}
