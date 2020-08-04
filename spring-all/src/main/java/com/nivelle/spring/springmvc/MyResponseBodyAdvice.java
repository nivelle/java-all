package com.nivelle.spring.springmvc;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fuxinzhong
 * @date 2020/08/03
 */
@ControllerAdvice
public class MyResponseBodyAdvice implements ResponseBodyAdvice<Map> {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        System.out.println(returnType.getParameterType());
        System.out.println("MyResponseBodyAdvice supports==》" + "\nreturnType：" + returnType + "\n converterType" + converterType + "\nsupports:" + returnType.getParameterType().equals(HashMap.class));
        return returnType.getParameterType().equals(HashMap.class);
    }

    @Override
    public Map beforeBodyWrite(Map body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        System.out.println("MyResponseBodyAdvice ==>body:" + body + "\n returnType:" + returnType + "\n selectedContentType:" +
                selectedContentType + "\n request:" + request + "\n response:" + response);
        body.put(3,3);
        return body;
    }
}
