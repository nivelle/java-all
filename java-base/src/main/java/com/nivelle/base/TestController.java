package com.nivelle.base;

import com.google.common.collect.Maps;
import com.nivelle.base.utils.GsonUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2020/04/03
 */
@Controller
@RequestMapping(value = "/test")
public class TestController {

    HashMap cache = Maps.newHashMap();

    @RequestMapping("/sayHello")
    public String config() {

        return "hello world";
    }


    /**
     * 请求中断
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     * @throws Exception
     */
    @RequestMapping("/writeData")
    @ResponseBody
    public void writeData(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            int i = 0;
            PrintWriter printWriter = httpServletResponse.getWriter();
            while (true) {
                Thread.sleep(1000);
                i++;
                printWriter.write(i);
                if (i%10==0)System.out.print(i);
            }
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    @RequestMapping("/url")
    public Object redirect() {
        RedirectView redirectTarget = new RedirectView();
        redirectTarget.setContextRelative(true);
        redirectTarget.setUrl("http://baidu.com");
        return redirectTarget;
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
     * consumes:指定处理 请求的提交内容类型（Content-Type），例如application/json, text/html;
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
    public String httpTest(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("http receive message");
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        String host = request.getHeader(HttpHeaders.HOST);
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String referer = request.getHeader(HttpHeaders.REFERER);
        String expect = request.getHeader(HttpHeaders.EXPECT);
        String cacheControl = request.getHeader(HttpHeaders.CACHE_CONTROL);
        System.out.println("cacheControl is:" + cacheControl);
        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        System.out.println("cache is:" + cache.get("value"));
        if (ifNoneMatch.equals(cache.get("value"))) {
            response.setHeader(HttpHeaders.ETAG, ifNoneMatch);
            //304 Not Modified
            response.setStatus(304);
        } else {
            cache.put("value", ifNoneMatch);
        }
        System.out.println("ifNoneMatch is:" + ifNoneMatch);
        return "http is ok and accept origin is:" + origin + ";host is:" + host + ";authorization is:" + authorization + ";accept is:" + accept + ";contentType is:" + contentType
                + ";userAgent=" + userAgent + ";referer is:" + referer + ";expect is:" + expect + ";cacheControl is:" + cacheControl;
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
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, baseStr);
        response.setStatus(401);
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
        //response.setStatus(201);
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
        /**
         * Content-disposition 是 MIME 协议的扩展，MIME 协议指示 MIME 用户代理如何显示附加的文件。
         *
         * Content-disposition其实可以控制用户请求所得的内容存为一个文件的时候提供一个默认的文件名，文件直接在浏览器上显示或者在访问时弹出文件下载对话框
         *
         */
        response.setHeader("", "attachment;filename=attachmentName.txt");
        response.getWriter().write(GsonUtils.toJson(result));
    }

    /**
     * request & response header
     */
    @RequestMapping(value = "/http4")
    public void httpTest4(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap result = Maps.newHashMap();

        String expect = request.getHeader(HttpHeaders.EXPECT);
        result.put(HttpHeaders.EXPECT, expect);
        if (expect.equals("100-continue")) {
            response.setHeader(HttpHeaders.EXPECT, expect);
        }
        System.out.println("result is:" + result);
        response.setHeader(HttpHeaders.ALLOW, HttpMethod.GET.name());
        //response.setStatus(405);
        result.put(HttpHeaders.ALLOW, HttpMethod.GET.name());
        String contentType = request.getContentType();
        /**
         * appliction/x-www-form-urlencoded:
         *
         * 它是post的默认格式，使用js中URLencode转码方法。包括将name、value中的空格替换为加号；将非ascii字符做百分号编码；将input的name、value用‘=’连接，不同的input之间用‘&’连接
         */
        if ("appliction/x-www-form-urlencoded".equals(contentType)) {
            Map<String, String[]> params = request.getParameterMap();
            System.out.println("urlencoded map is:" + params);

        } else if ("application/json".equals(contentType)) {
            String paramJson = IOUtils.toString(request.getInputStream(), "utf-8");
            Map jsonMap = GsonUtils.fromJsonFormat(paramJson, Map.class);
            String dataStr = (String) jsonMap.get("data");
            System.out.println("字节数：" + dataStr.getBytes().length);

            /**
             * multipart/form-data:
             *
             * 对于一段utf8编码的字节，用application/x-www-form-urlencoded传输其中的ascii字符没有问题，但对于非ascii字符传输效率就很低了（汉字‘丁’从三字节变成了九字节），因此在传很长的字节（如文件）时应用multipart/form-data格式。smtp等协议也使用或借鉴了此格式。
             *
             * multipart/form-data将表单中的每个input转为了一个由boundary分割的小格式，没有转码，直接将utf8字节拼接到请求体中，在本地有多少字节实际就发送多少字节，极大提高了效率，适合传输长字节
             *
             */
        } else if ("multipart/form-data".equals(contentType)) {

            /**
             * application/octet-stream:
             *
             * 当你在响应类型为application/octet- stream情况下使用了这个头信息的话，那就意味着你不想直接显示内容，而是弹出一个"文件下载"的对话框，接下来就是由你来决定"打开"还是"保存" 了
             */
        } else if ("application/octet-stream".equals(contentType)) {

        }
        response.getWriter().write(GsonUtils.toJson(result));
    }

    @RequestMapping(value = "http5")
    public String httpTest5(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("http receive message");
        return "ok";
    }
}
