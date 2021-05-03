package com.nivelle.spring.springmvc.servlet;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SpringMVC servlet底层实现
 *
 * @author fuxinzhong
 * @date 2021/05/04
 */
public class SpringMVCServlet {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(5));

    @PostMapping("mvcServlet")
    public DeferredResult<String> listPostDeferredResult() {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        executor.execute(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("异步任务执行。。。完毕");
                deferredResult.setResult("ok");
            } catch (Exception e) {
                deferredResult.setErrorResult(e.getMessage());
            }
        });
        return deferredResult;
    }
}
