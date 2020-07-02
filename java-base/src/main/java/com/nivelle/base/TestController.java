package com.nivelle.base;

import com.google.common.collect.Maps;
import com.nivelle.base.utils.GsonUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivell
 * @date 2020/04/03
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {


    @RequestMapping("/sayHello")
    public String config() {

        return "hello world";
    }

    /**
     * 1、RequestMapping接口的源码如下，里面定义了七个属性
     * <p>
     * （1）@Target中有两个属性，分别为 ElementType.METHOD 和 ElementType.TYPE ，也就是说 @RequestMapping 可以在方法和类的声明中使用
     * <p>
     * （2）可以看到注解中的属性除了 name() 返回的字符串，其它的方法均返回数组，也就是可以定义多个属性值，例如 value()可以同时定义多个字符串值来接收多个URL请求；
     * <p>
     * （3）value， method；
     * <p>
     * value： 指定请求的实际地址，指定的地址可以是URI Template 模式（后面将会说明）；
     * <p>
     * method： 指定请求的method类型， GET、POST、PUT、DELETE等；
     * <p>
     * （4）consumes，produces；
     * <p>
     * consumes： 指定处理 请求的提交内容类型（Content-Type），例如application/json, text/html;
     * <p>
     * produces: 指定返回的内容类型，仅当request请求头中的(Accept)类型中包含该指定类型才返回;
     * <p>
     * （5） params，headers；
     * <p>
     * params： 指定request中必须包含某些参数值是，才让该方法处理。
     * <p>
     * headers： 指定request中必须包含某些指定的header值，才能让该方法处理请求。
     */
    @RequestMapping(value = "http")
    public String httpTest(HttpServletRequest request) {
        System.out.println("http receive message");
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        System.out.println("origin is:" + origin);
        return "http is ok and accept";
    }

    /**
     * httpDemo
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/http2")
    public void httpTest2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /**
         * 获取鉴权信息
         */
        String authorization = request.getHeader("Authorization");
        String baseStr = "Basic " + Base64.getEncoder().encodeToString("nivelle:fuxinzhong2".getBytes());
        System.out.println("header:" + authorization);
        System.out.println("local:" + baseStr);
        if (!authorization.equals(baseStr)) {
            System.out.println("fail");
            return;
        }
        Cookie[] cookies = request.getCookies();
        System.out.println(cookies != null ? "cookie key is " + cookies[0].getName() + "  value is " + cookies[0].getValue() : "cookies为空");
        if (cookies != null) {
            if (!cookies[0].getValue().equals("nivelle")) {
                System.out.println("cookie 被修改了");
                return;
            }
        }
        String param1 = request.getParameter("param1");
        String headerAccept = request.getHeader("accept");
        response.setHeader("connection", "close");
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        System.out.println("request header content-Type:" + contentType);
        /**
         * 返回内容的MIME类型
         */
        response.setContentType("text/html");
        /**
         * 返回内容的编码
         */
        response.setCharacterEncoding("utf-8");
        /**
         * 设置cookie值
         */
        Cookie cookie = new Cookie("name", "nivelle");
        response.addCookie(cookie);
        HashMap result = Maps.newHashMap();
        result.put("param1", param1);
        result.put("parma2", headerAccept);
        /**
         * 设置 请求资源可替代的备用j 的另一地址
         */
        response.setHeader("Content-Location", "http://nivelle.me");
        /**
         * Location 对应的是响应,而Content-Location对应的是要返回的实体
         */
        response.setHeader(HttpHeaders.LOCATION, "http://127.0.0.1:8090/base/test/http");
        /**
         * "127.0.0.1 - - [30/Jun/2020:22:31:32 +0800] "POST /base/test/http2 HTTP/1.1" 308 16 "-" "PostmanRuntime/7.26.1" - "1359ms""
         * "127.0.0.1 - - [30/Jun/2020:22:31:32 +0800] "POST /base/test/http HTTP/1.1" 200 21 "http://127.0.0.1:8090/base/test/http2" "PostmanRuntime/7.26.1" - "46ms""
         *
         * code: 303 (See Also) 始终引致请求使用 GET 方法，而，而 307 (Temporary Redirect) 和 308 (Permanent Redirect) 则不转变初始请求中的所使用的方法
         *
         * 除了重定向响应之外， 状态码为 201 (Created) 的消息也会带有Location首部。它指向的是新创建的资源的地址。
         */
        response.setStatus(201);
        response.getWriter().write(GsonUtils.toJson(result));
    }

    /**
     * sendRedirect 默认302
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/http3")
    public void httpTest3(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap result = Maps.newHashMap();
        /**
         * "127.0.0.1 - - [30/Jun/2020:22:27:58 +0800] "POST /base/test/http3 HTTP/1.1" 302 - "-" "PostmanRuntime/7.26.1" - "5ms""
         * "127.0.0.1 - - [30/Jun/2020:22:27:58 +0800] "GET /base/test/http2 HTTP/1.1" 200 16 "http://127.0.0.1:8090/base/test/http3" "PostmanRuntime/7.26.1" - "7ms""
         */
        response.sendRedirect("http://127.0.0.1:8090/base/test/http2");
        response.getWriter().write(GsonUtils.toJson(result));
    }

    /**
     * request & response header
     */
    @RequestMapping(value = "/http4")
    public void httpTest4(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap result = Maps.newHashMap();
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        result.put(HttpHeaders.USER_AGENT, userAgent);

        String origin = request.getHeader(HttpHeaders.ORIGIN);
        result.put(HttpHeaders.ORIGIN, origin);

        String referer = request.getHeader(HttpHeaders.REFERER);
        result.put(HttpHeaders.REFERER, referer);

        String expect = request.getHeader(HttpHeaders.EXPECT);
        result.put(HttpHeaders.EXPECT, expect);
        if (expect.equals("100-continue")){
            response.setHeader(HttpHeaders.EXPECT, expect);
        }
        System.out.println("result is:" + result);
        response.setHeader(HttpHeaders.ALLOW, HttpMethod.GET.name());
        //response.setStatus(405);
        result.put(HttpHeaders.ALLOW, HttpMethod.GET.name());
        String contentType = request.getContentType();

        if ("appliction/x-www-form-urlencoded".equals(contentType)) {

        } else if ("application/json".equals(contentType)) {
            String paramJson = IOUtils.toString(request.getInputStream(), "utf-8");
            Map jsonMap = GsonUtils.fromJsonFormat(paramJson, Map.class);
            String dataStr = (String) jsonMap.get("data");
            System.out.println("字节数：" + dataStr.getBytes().length);

        }
        response.getWriter().write(GsonUtils.toJson(result));
    }
}
