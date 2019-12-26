package com.nivelle.spring.springmvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Properties;

/**
 * springMNC参数绑定学习
 */
@RequestMapping("test/springMVC")
@Controller
public class ParameterBindController {

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
     * {@link PropertiesHandlerMethodArgumentResolver}
     * {@link PropertiesHandlerMethodReturnValueHandler}
     *
     * @param properties
     * @return
     */
    @RequestMapping(value = "convertSelf", consumes = "text/properties")
    public Properties getconvertSelf(Properties properties) {
        System.out.println("入参被解析:properties={}" + properties);
        return properties;
    }

    /**
     * 自定义参数转换器测试,依赖@RequestBody和 @ResponseBody
     * <p>
     * {@link PropertiesHttpMessageConverter}
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


}
