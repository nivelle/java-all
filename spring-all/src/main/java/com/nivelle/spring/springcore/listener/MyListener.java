package com.nivelle.spring.springcore.listener;

import com.nivelle.spring.springcore.event.MyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 自定义事件监听器
 *
 * @author nivelle
 * @date 2019/08/23
 */
@Component
public class MyListener implements ApplicationListener<MyEvent> {
    @Override
    public void onApplicationEvent(MyEvent event) {
        System.out.println("监听到自定义事件:" + event.getClass());
        event.printEventContent("自定义事件输入内容！！！！");
    }
}
