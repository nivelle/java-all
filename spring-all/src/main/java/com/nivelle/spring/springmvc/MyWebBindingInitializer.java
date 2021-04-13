package com.nivelle.spring.springmvc;

import com.nivelle.spring.pojo.User;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;

/**
 * 、
 *
 * @author fuxinzhong
 * @date 2020/08/16
 */
public class MyWebBindingInitializer implements WebBindingInitializer {
    @Override
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(User.class, new MyPropertyEditorSupport());
    }
}
