package com.nivelle.guide.springmvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * springMNC参数绑定学习
 */
@RequestMapping("springMVC")
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
     * @param session
     * @return
     */
    @RequestMapping("/session")
    public ModelAndView getSession(HttpSession session, Cookie cookie) {

        String sessionContent = session.getAttribute("sessionParameter").toString();
        System.out.println("====="+sessionContent);

        System.out.println("-----"+cookie.getValue());

        ModelAndView mv = new ModelAndView();
        mv.setViewName("/success");
        return mv;
    }
}
