package com.nivelle.spring.controllor;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 文档导出
 *
 * @author nivelle
 * @date 2020/04/23
 */
@Controller
@RequestMapping("test/freeMarker")
public class FreemakerController {

    // 处理下载word文档
    @RequestMapping("/download")
    public void downloadWord(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/msword");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=违纪处分通知.doc");
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("persons", 1000000);
            //创建配置实例对象
            Configuration configuration = new Configuration();
            //设置编码
            configuration.setDefaultEncoding("UTF-8");
            //加载需要装填的模板
            configuration.setClassForTemplateLoading(this.getClass(), "/templates");
            //设置对象包装器
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            //设置异常处理器
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
            //获取ftl模板对象
            Template template = configuration.getTemplate("图书馆使用报告.ftl");
            //输出文档
            StringBuilder fileName = new StringBuilder("");
            fileName.append("掌阅科技").append("_").append("图书馆使用报告").append(".doc");
            try {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment;filename="
                        + new String(fileName.toString().getBytes("GBK"), "ISO-8859-1"));
                response.setCharacterEncoding("utf-8");//处理乱码问题
                //生成Word文档
                template.process(dataMap, response.getWriter());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                response.flushBuffer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
