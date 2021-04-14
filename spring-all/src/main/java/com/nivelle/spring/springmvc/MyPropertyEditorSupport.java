package com.nivelle.spring.springmvc;

import com.nivelle.spring.pojo.User;

import java.beans.PropertyEditorSupport;

/**
 * 自定义属性转换器(类型转换的一种)
 * <p>
 * 通过 WebBindingInitializer 注入自定义的类型处理器
 *
 * @author fuxinzhong
 * @date 2020/08/16
 */
public class MyPropertyEditorSupport extends PropertyEditorSupport {
    /**
     * 需要SpringMVC为我们自动进行类型转换的时候都是用的PropertyEditor。通过PropertyEditor的setAsText()方法我们可以实现字符串向特定类型的转换。
     * 但是这里有一个限制是它只支持从String类型转为其他类型
     * @param text
     * @throws IllegalArgumentException
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        //解析逗号分割字符串的内容
        if (text.indexOf(",") > 0) {
            User user = new User();
            String[] arr = text.split(",");
            user.setName(arr[0]);
            user.setAge(Integer.parseInt(arr[1]));
            setValue(user);
        } else {
            throw new IllegalArgumentException("user param is error");
        }
    }
}
