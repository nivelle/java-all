package com.nivelle.spring.controllor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nivelle.spring.configbean.CommonConfig;
import com.nivelle.spring.pojo.*;
import com.nivelle.spring.springboot.dao.ActivityDaoImpl;
import com.nivelle.spring.springboot.mapper.ActivityPvMapper;
import com.nivelle.spring.springcore.aop.MyService;
import com.nivelle.spring.springcore.lifecycle.InitSpringBean;
import com.nivelle.spring.springcore.lifecycle.XmlBean;
import com.nivelle.spring.springcore.factorybean.*;
import com.nivelle.spring.springcore.event.MyEvent;
import com.nivelle.spring.springmvc.MyHandlerMethodArgumentResolver;
import com.nivelle.spring.springmvc.MyHandlerMethodReturnValueHandler;
import com.nivelle.spring.springmvc.MyHttpMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.*;

@Controller
public class SpringAllTestController implements ApplicationContextAware {

    @Autowired
    CommonConfig commonConfig;
    @Autowired
    ActivityPvMapper activityPvMapper;
    @Autowired
    ActivityDaoImpl activityDao;
    @Autowired
    MyFactoryBean myFactoryBean;

    @Autowired
    MyService myService;
    @Autowired
    InitSpringBean initSpringBean;
    /**
     * 获取上下文
     */
    @Autowired
    WebApplicationContext webApplicationConnect;


    ApplicationContext applicationContext;

    ///springMVC核心注解

    /**
     * 获取某个请求头
     *
     * @param contentType
     * @return
     * @RequestHeader
     */
    @PostMapping("/header")
    @ResponseBody
    public Object configHeader(@RequestHeader(name = HttpHeaders.CONTENT_TYPE) String contentType) {
        System.out.println(contentType);
        return contentType + "aa";
    }

    /**
     * 获取所有的请求头
     *
     * @param headers
     * @return
     * @RequestHeader
     */
    @PostMapping("/headers")
    @ResponseBody
    public Object configHeaders(@RequestHeader Map headers) {
        System.out.println(headers);
        return headers;
    }

    /**
     * 本质上是利用了 @RequestParam defaultValue 持占位符和SpEL的特性
     *
     * @return
     * @Value
     */
    @PostMapping("/value")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "瞎几吧搞")
    public Object value(@Value(value = "${myConfig.desc}") String name) {
        System.out.println(name);
        return name + "return";
    }

    /**
     * MethodArgumentResolver
     *
     * @param user
     * @return
     */
    @PostMapping("/config")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_GATEWAY)
    public Object config(@RequestBody User user) {
        String desc = commonConfig.getDesc();
        System.out.println(desc);
        System.out.println(user.name);
        HashMap result = Maps.newHashMap();
        result.put("age", user.getAge());
        result.put("name", user.getName());
        return result;
    }

    /**
     * pathValue 非必填
     *
     * @param id
     * @return
     */
    @GetMapping({"/pathValue/{id}", "/pathValue"})
    @ResponseBody
    public Object pathValue(@PathVariable(required = false) String id) {
        HashMap result = Maps.newHashMap();
        result.put("id", id);
        return result;
    }

    /**
     * Map参数封装
     *
     * @return
     */
    @PostMapping("/config2")
    @ResponseBody
    public Object config2(@RequestParam Map params) {
        System.out.println(params);
        System.out.println(params.get("name"));
        HashMap result = Maps.newHashMap();
        result.put("name", params.get("name") + "fuck");
        return result;
    }

    /**
     * @return
     * @RequestParam value 支持占位符
     */
    @PostMapping("/config3")
    @ResponseBody
    public Object config3(@RequestParam(value = "${myConfig.desc}") String desc) {
        HashMap result = Maps.newHashMap();
        result.put("desc", desc);
        return result;
    }

    /**
     * 参数映射前拦截器: requestBodyAdvice
     *
     * @return
     */
    @RequestMapping("/argument")
    @ResponseBody
    public String argument() {
        return "nivelle";
    }

    /**
     * 返回值返回前拦截器: responseBodyAdvice
     *
     * @return
     */
    @RequestMapping("/return")
    @ResponseBody
    public Map returnValue() {
        Map map = Maps.newHashMap();
        map.put(1, 2);
        return map;
    }

    /**
     * MyHttpMessageConvert 测试
     *
     * @return
     */
    @RequestMapping("/return2")
    @ResponseBody
    public User returnValue2() {
        User user = new User();
        user.setAge(1);
        user.setName("nivelle");
        return user;
    }

    @RequestMapping("/return3")
    @ResponseBody
    public Properties returnValue3() {
        Properties properties = new Properties();
        User user = new User();
        user.setAge(1);
        user.setName("nivelle");
        properties.setProperty("user", user.toString());
        return properties;
    }

    /**
     * 自定义事件发布
     *
     * @return
     */
    @RequestMapping("/publishEvent")
    public void publishEvent() {
        webApplicationConnect.publishEvent(new MyEvent("你好"));
        return;
    }

    /**
     * 单实例测试
     * singleBean 测试
     */
    @RequestMapping("/singletonBean")
    public Object singletonTest() {
        Dog dog = (Dog) webApplicationConnect.getBean("bigDog");
        System.out.println(dog.getClass().getName());
        Dog dog2 = (Dog) webApplicationConnect.getBean("bigDog");
        if (dog == dog2) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * prototype bean 多实例测试
     */
    @RequestMapping("/prototypeBean")
    public Object prototypeBeanTest() {
        Dog dog = (Dog) webApplicationConnect.getBean("buDingDog");
        Dog dog2 = (Dog) webApplicationConnect.getBean("buDingDog");
        if (dog == dog2) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 从webApplicationConnect容器获取指定bean
     *
     * @return
     */
    @RequestMapping("/registerBean")
    public Object contentBean() {
        /**
         * springboot默认属性未设置值时为null,可设置为""
         */
        Object object = webApplicationConnect.getBean("userInfo");
        return object;
    }

    /**
     * 通过xml配置文件导入不能自动扫描到的实例
     *
     * @return
     */
    @RequestMapping("xml")
    public Object xmlService() {
        Object xmlBeanService = webApplicationConnect.getBean("xmlService");
        XmlBean xmlBeanService1 = (XmlBean) xmlBeanService;
        return xmlBeanService1.helloXmlService();
    }


    /**
     * Spring 初始化测试
     *
     * @return
     */
    @RequestMapping("/init")
    @ResponseBody
    public Object myInitSpringBean() {
        String name = initSpringBean.getName();
        int age = initSpringBean.getAge();
        return name + age;
    }

    /**
     * 参数获取
     *
     * @param request
     * @param response
     * @param session
     * @param model
     * @param modelMap
     * @return
     * @throws Exception
     */
    @RequestMapping("/defaultParameter")
    public ModelAndView defaultParameter(HttpServletRequest request,
                                         HttpServletResponse response,
                                         HttpSession session,
                                         Model model,
                                         ModelMap modelMap) throws Exception {
        request.setAttribute("requestParameter", "request类型");
        response.getWriter().write("nivelle's response");
        session.setAttribute("sessionParameter", "session类型");
        //ModelMap是Model接口的一个实现类，作用是将Model数据填充到request域
        //即使使用Model接口，其内部绑定还是由ModelMap来实现
        model.addAttribute("modelParameter", "model类型");
        modelMap.addAttribute("modelMapParameter", "modelMap类型");
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/success");
        return mv;
    }

    /**
     * session/cookie
     *
     * @param session
     * @return
     */
    @RequestMapping("/session/cookie")
    public ModelAndView getSession(HttpSession session, HttpServletRequest request) {
        String sessionContent = session.getAttribute("sessionParameter").toString();
        System.out.println("=====" + sessionContent);
        System.out.println("-----" + request.getCookies()[0].getName() + request.getCookies()[0].getValue());
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/success");
        return mv;
    }

    /**
     * 自定义参数转换器测试,不依赖@RequestBody和 @ResponseBody
     * <p>
     * {@link MyHandlerMethodArgumentResolver}
     * {@link MyHandlerMethodReturnValueHandler}
     *
     * @param properties
     * @return
     */
    @RequestMapping(value = "convertSelf", consumes = "text/properties")
    public Properties getConvertSelf(Properties properties) {
        System.out.println("入参被解析:properties={}" + properties);
        return properties;
    }

    /**
     * 自定义参数转换器测试,依赖@RequestBody和 @ResponseBody
     * <p>
     * {@link MyHttpMessageConverter}
     *
     * @param properties
     * @return
     */
    @RequestMapping(value = "convertAnnotation", produces = "text/properties", consumes = "text/properties")
    @ResponseBody
    public Properties getConvertAnnotation(@RequestBody Properties properties) {
        System.out.println("入参被解析:properties的类型:" + properties.getClass().getSimpleName());
        return properties;
    }


    /**
     * 工厂类获取bean
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/timeline")
    public List<Object> getObject() throws Exception {
        //直接通过#getObject获取实例
        TimeLine timeLine = myFactoryBean.getObject();

        //通过Spring上下文获取实例
        TimeLine timeLine1 = (TimeLine) applicationContext.getBean("myFactoryBean");
        //MyFactoryBean
        MyFactoryBean bean = (MyFactoryBean) applicationContext.getBean("&myFactoryBean");
        List<Object> time = Lists.newArrayList();
        time.add(timeLine);
        time.add(timeLine1);
        time.add(bean);
        return time;
    }

    /**
     * 使用AOP代理
     *
     * @return
     */
    @RequestMapping("myService")
    public String writeLog() {
        myService.writeLog();
        return "SUCCESS";
    }

    /**
     * 模拟在写出数据的过程中中断连接
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
                if (i % 10 == 0) {
                    System.out.print(i);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * 类型转换器测试
     *
     * @param source
     */
    @RequestMapping("/converter")
    @ResponseBody
    public Object converter(Boolean source) {
        /**
         *      trueValues.add("true");
         * 		trueValues.add("on");
         * 		trueValues.add("yes");
         * 		trueValues.add("1");
         */
        System.err.println(source);
        System.err.println(source);
        System.err.println(source);
        return source;
    }


    /**
     * 类型转换器测试
     *
     * @param source
     */
    @RequestMapping("/converter2")
    @ResponseBody
    public Object converter2(Date source) {
        System.err.println(source);
        System.err.println(source);
        System.err.println(source);
        return source;
    }

    /**
     * 类型转换器测试
     *
     * @param user
     */
    @RequestMapping("/converter3")
    @ResponseBody
    public Object converter3(@RequestBody User user) {
        System.err.println(user);
        System.err.println(user);
        System.err.println(user);
        return user;
    }

    /**
     * 默认加载类
     *
     * @para
     */
    @RequestMapping("/defaultBeans")
    @ResponseBody
    public Object defaultBeans() {
        String[] defaultBeans = applicationContext.getBeanDefinitionNames();
        System.out.println(defaultBeans.length);
        return defaultBeans;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = applicationContext;
    }
}
