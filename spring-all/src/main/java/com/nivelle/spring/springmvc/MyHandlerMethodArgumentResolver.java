package com.nivelle.spring.springmvc;


import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * 自定义入参参数转换器
 *
 * @Author nivelle
 */
public class MyHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 是否支持parameter指定的方法参数
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        System.out.println("parameter:" + parameter.getParameter());
        System.out.println("parameter name1:" + parameter.getParameter().getName());
        System.out.println("parameter type:" + parameter.getParameterType());
        System.out.println("parameter name2:" + parameter.getParameterName());
        System.out.println("parameter executable:" + parameter.getExecutable());
        System.out.println("parameter method:" + parameter.getMethod());
        System.out.println("HandlerMethodArgumentResolver ==>supportsParameter,\n parameter=" + parameter + "\n supportsParameter:" + (Properties.class.equals(parameter.getParameterType())));
        return Properties.class.equals(parameter.getParameterType());
    }

    /**
     * 从指定请求上下文中，将方法参数MethodParameter解析为参数值。这里需要解析的参数parameter一定符合如下条件:将其交给当前HandlerMethodArgumentResolver对象的方法supportsParameter,返回结果是true
     * <p>
     * 它是HandlerMethod方法的解析器,将HttpServletRequest(header + body 中的内容)解析为HandlerMethod方法的参数（method parameters）
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        System.out.println("HandlerMethodArgumentResolver ==>resolveArgument,\n parameter=" + parameter + "\n mavContainer:" + mavContainer + "\n webRequest :" + webRequest + "\n binderFactory:" + binderFactory);
        ServletWebRequest servletWebRequest = (ServletWebRequest) webRequest;
        HttpServletRequest request = servletWebRequest.getRequest();
        String contentType = request.getHeader("Content-Type");
        MediaType mediaType = MediaType.parseMediaType(contentType);
        // 获取编码
        Charset charset = mediaType.getCharset() == null ? Charset.forName("UTF-8") : mediaType.getCharset();
        // 获取输入流
        InputStream inputStream = request.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
        // 输入流转换为 Properties
        Properties properties = new Properties();
        properties.load(inputStreamReader);
        return properties;
    }
}

