package com.nivelle.spring.springmvc;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author fuxinzhong
 * @date 2020/08/03
 */
@ControllerAdvice
public class MyResponseBodyAdvice implements ResponseBodyAdvice<Properties> {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        System.out.println("MyResponseBodyAdvice supports==》" + "\nreturnType：" + returnType + "\n converterType" + converterType + "\nsupports:" + returnType.getParameterType().equals(Properties.class));
        return returnType.getParameterType().equals(Properties.class);
    }

    @Override
    public Properties beforeBodyWrite(Properties body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        System.out.println("MyResponseBodyAdvice ==>body:" + body + "\n returnType:" + returnType + "\n selectedContentType:" +
                selectedContentType + "\n request:" + request + "\n response:" + response);
        body.put("adviceValue", "fuck");
        return body;
    }
}
