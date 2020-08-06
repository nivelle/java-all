package com.nivelle.spring.springmvc;

import com.nivelle.spring.pojo.User;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/08/03
 */
@ControllerAdvice
public class MyRequestBodyAdvice implements RequestBodyAdvice {
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        System.out.println("parameter:" + methodParameter.getParameter());
        System.out.println("parameter name1:" + methodParameter.getParameter().getName());
        System.out.println("parameter type:" + methodParameter.getParameterType());
        System.out.println("parameter name2:" + methodParameter.getParameterName());
        System.out.println("parameter executable:" + methodParameter.getExecutable());
        System.out.println("parameter method:" + methodParameter.getMethod());
        System.out.println("parameter toString:" + methodParameter.getParameter().toString());
        System.out.println("parameter modifiers:" + methodParameter.getParameter().getModifiers());

        System.out.println("MyRequestBodyAdvice support==》methodParameter：" + methodParameter + "\n targetType:" + targetType + "\n converterType:" + converterType + "\n support is:" + methodParameter.getParameterType().equals(User.class));
        return methodParameter.getParameterType().equals(User.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        System.out.println("MyRequestBodyAdvice beforeBodyRead ==>inputMessage:"
                + inputMessage + "\n parameter:" + parameter + "\n targetType:" + targetType + "\n converterType:" + converterType);
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        System.out.println("MyRequestBodyAdvice afterBodyRead ==>body:"
                + body + "\n inputMessage:" + inputMessage + "\n parameter:" + parameter + "\n targetType:" + targetType + "\n converterType:" + converterType);
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        System.out.println("MyRequestBodyAdvice handleEmptyBody==>body:" + body + "\n inputMessage:" + inputMessage + "\n parameter:" + parameter + "\n targetType:" + targetType + "\n converterType:" + converterType);
        return body;
    }
}

