package com.nivelle.spring.springmvc;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * 返回值参数类型转换
 *
 * @Author nivelle
 */
public class MyHandlerMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        System.out.println("HandlerMethodReturnValueHandler ==>supportsReturnType \n returnType:"+returnType+"\n supportsReturnType:"+Properties.class.equals(returnType.getMethod().getReturnType()));
        return Properties.class.equals(returnType.getMethod().getReturnType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        System.out.println("HandlerMethodReturnValueHandler ==>handleReturnValue \n returnValue:"+returnValue+"\n returnType:"+returnType+"\n mavContainer :"+mavContainer+"\n webRequest:"+webRequest);
        Properties properties = (Properties) returnValue;
        ServletWebRequest servletWebRequest = (ServletWebRequest) webRequest;
        HttpServletResponse response = servletWebRequest.getResponse();
        ServletServerHttpResponse servletServerHttpResponse = new ServletServerHttpResponse(response);
        // 获取请求头
        HttpHeaders headers = servletServerHttpResponse.getHeaders();
        MediaType contentType = headers.getContentType();
        // 获取编码
        Charset charset = null;
        if (contentType != null) {
            charset = contentType.getCharset();
        }
        charset = charset == null ? Charset.forName("UTF-8") : charset;
        // 获取请求体
        OutputStream body = servletServerHttpResponse.getBody();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(body, charset);
        properties.store(outputStreamWriter, "自定义：properties-HandlerMethodReturnValueHandler-handleReturnValue");
        // 告诉 Spring MVC 请求已经处理完毕
        mavContainer.setRequestHandled(true);
    }
}