package com.nivelle.spring.springmvc.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 跨域过滤器处理
 *
 * @author nivelle
 * @date 2019/08/15
 */
public class MyCrossFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("跨域请求配置");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        // 配置*不生效
        String[] allowDomain =
                {"http://localhost:8080", "http://localhost:8081", "http://localhost:8088", "http://localhost:8090"};
        Set allowedOrigins = new HashSet(Arrays.asList(allowDomain));
        String originHeader = ((HttpServletRequest) req).getHeader("Origin");
        System.err.println("CorsFilter originHeader=" + originHeader);
        System.err.println("判断是否在允许跨域的列表里" + allowedOrigins.contains(originHeader));
        if (allowedOrigins.contains(originHeader)) {
            response.setHeader("Access-Control-Allow-Origin", originHeader);
            /**
             * Credentials(许可,证书): 该字段可选。它的值是一个布尔值，表示是否允许发送Cookie。默认情况下，Cookie不包括在CORS请求之中。
             * 设为true，即表示服务器明确许可，Cookie可以包含在请求中，一起发给服务器。这个值也只能设为true，如果服务器不要浏览器发送Cookie，删除该字段即可。
             */
            response.setHeader("Access-Control-Allow-Credentials", "true");
            /**
             * 该字段必需，它的值是逗号分隔的一个字符串，表明服务器支持的所有跨域请求的方法。
             * 注意，返回的是所有支持的方法，而不单是浏览器请求的那个方法。这是为了避免多次"预检"请求。
             */
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            /**
             * 该字段可选，用来指定本次预检请求的有效期，单位为秒。上面结果中，有效期是20天（1728000秒），即允许缓存该条回应1728000秒（即20天），在此期间，不用发出另一条预检请求。
             */
            response.setHeader("Access-Control-Max-Age", "3600");
            /**
             * 如果浏览器请求包括Access-Control-Request-Headers字段，则Access-Control-Allow-Headers字段是必需的。
             * 它也是一个逗号分隔的字符串，表明服务器支持的所有头信息字段，不限于浏览器在"预检"中请求的字段。
             */
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, x-requested-with, X-Custom-Header, Authorization, X-System-ID, X-Token, C-User");
            System.out.println("完成了向响应信息里面加入跨域信息");
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {

    }
}
