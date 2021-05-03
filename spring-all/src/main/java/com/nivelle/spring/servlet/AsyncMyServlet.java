package com.nivelle.spring.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/05/03
 */
@WebServlet(urlPatterns = "/myAsyncMyServlet", asyncSupported = true)
public class AsyncMyServlet extends HttpServlet {
    private final static int AVALLABLE_PROCESS = Runtime.getRuntime().availableProcessors();
    private final static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(AVALLABLE_PROCESS * 2, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(5), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
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
        threadPoolExecutor.execute(() -> {
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

        System.out.println("end servlet.....");
    }

}
