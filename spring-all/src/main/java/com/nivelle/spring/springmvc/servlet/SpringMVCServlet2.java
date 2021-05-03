package com.nivelle.spring.springmvc.servlet;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SpringMVC servlet底层实现
 *
 * @author fuxinzhong
 * @date 2021/05/04
 */
public class SpringMVCServlet2 {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(5));

    @PostMapping("mvcServlet2")
    public Callable<String> listPostDeferredResult() {
       return new Callable<String>() {
           @Override
           public String call() throws Exception {
               System.out.println("mvcServlet2 is done");
               return "ok mvcServlet2";
           }
       };
    }
}
