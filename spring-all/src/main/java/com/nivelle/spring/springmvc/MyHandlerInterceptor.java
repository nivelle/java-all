package com.nivelle.spring.springmvc;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring MVC框架对AOP的一种实现方式
 * 一般简单的功能又是通用的,每个请求都要去处理的,比如判断token是否失效可以使用spring mvc的 HanlderInterceptor,
 * 复杂的,比如缓存,需要高度自定义的就用spring aop。
 * 一般来说service层更多用spring aop，controller层有必要用到request和response的时候，可以用拦截器。
 *
 * @author nivellefu
 */
public class MyHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object o) {
        String localName = httpServletRequest.getLocalName();
        System.out.println("MyHandlerInterceptor =>preHandle 执行方法前执行返回的结果决定是否往下执行" + localName);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) {
        System.out.println("MyHandlerInterceptor => postHandle当方法返回值时执行");
        return;
    }


    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, Object o, Exception e) {
        System.out.println("afterCompletion无论成功或失败都将执行，前提是preHandler要返回true。");
    }
}
