package com.nivelle.guide.springboot.filter;

import javax.servlet.*;
import java.io.IOException;

public class MyFilter2 implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            System.out.println("过滤器初始化2");
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            System.out.println("开始执行过滤器2");
            String characterEncoding = servletRequest.getCharacterEncoding();
            int serverPort = servletRequest.getServerPort();

            System.out.println("characterEncoding is "+ characterEncoding +"\n"+ "serverPort is "+ serverPort);
            filterChain.doFilter(servletRequest, servletResponse);
            System.out.println("结束执行过滤器2");
        }

        @Override
        public void destroy() {
            System.out.println("过滤器销毁2");
        }

    }