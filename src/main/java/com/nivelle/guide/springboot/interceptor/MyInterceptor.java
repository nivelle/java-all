package com.nivelle.guide.springboot.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse,Object o) throws Exception{
        String localName = httpServletRequest.getLocalName();
        System.out.println("拦截器前置执行：localName="+localName);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView)throws Exception {
        System.out.println("开始执行拦截器");//sout+回车
        return;
    }


    @Override
    public void  afterCompletion(HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse, Object o, Exception e){
        System.out.println("无论被拦截的方法抛出异常与否都会执行");
    }
}
