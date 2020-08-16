package com.nivelle.spring.springmvc;

import com.nivelle.spring.pojo.User;

import java.beans.PropertyEditorSupport;

/**
 * 自定义属性转换器
 *
 * @author fuxinzhong
 * @date 2020/08/16
 */
public class MyPropertyEditorSupport extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text.indexOf(",")>0){
            User user = new User();
            String[] arr = text.split(",");
            user.setName(arr[0]);
            user.setAge(Integer.parseInt(arr[1]));
            setValue(user);
        }else {
            throw new IllegalArgumentException("user param is error");
        }
    }
}
