package com.nivelle.spring.springmvc.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 非阻塞IO实现
 *
 * @author fuxinzhong
 * @date 2021/05/03
 */
public class UnblockMyServlet extends HttpServlet {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue(5));

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("begin servlet.....");
        final AsyncContext asyncContext = request.startAsync();
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                System.out.println("onComplete");
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
                System.out.println("onTimeout");

            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {
                System.out.println("onError");
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
                System.out.println("onStartAsync");
            }
        });

            final ServletInputStream servletInputStream = request.getInputStream();
            servletInputStream.setReadListener(new ReadListener() {
                @Override
                public void onDataAvailable() throws IOException {
                    System.out.println("当有数据可读时，读取数据");
                    final ServletInputStream inputStream = asyncContext.getRequest().getInputStream();
                    try {
                        byte buffer[] = new byte[1024];
                        int readBytes = 0;
                        while (inputStream.isReady() && !inputStream.isFinished()) {
                            readBytes += inputStream.read(buffer);
                        }
                    } catch (Exception e) {
                    }
                }

            @Override
            public void onAllDataRead () throws IOException {
                System.out.println("所有数据读取完毕后");
                executor.execute(() -> {
                    try {
                        Thread.sleep(3000);
                        response.setContentType("text/html");
                        PrintWriter out = asyncContext.getResponse().getWriter();
                        out.println("<html>");
                        out.println("<head>");
                        out.println("<title>hello world</title>");
                        out.println("<body>");
                        out.println("<h1>this is my page</h1>");
                        out.println("</body>");
                        out.println("</body>");
                        out.println("</html>");
                    } catch (Exception e) {

                    } finally {
                        asyncContext.complete();
                    }
                });
            }

            @Override
            public void onError (Throwable throwable){

            }
        });


        System.out.println("end servlet.....");
    }
}
