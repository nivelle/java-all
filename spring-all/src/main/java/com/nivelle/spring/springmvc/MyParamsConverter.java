package com.nivelle.spring.springmvc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 自定义类型转换
 * <p>
 * Spring3引入了较Spring2的PropertyEditor更加强大、通用的Convert/Format SPI，
 * <p>
 * Convert SPI可以实现任意类型的转换；Format SPI支持国际化，并在前者的基础上实现了String与任意类型的转换。
 * <p>
 * 这两类SPI属于spring-core，被整个spring-framework共享，是一种通用的类型转换器。
 *
 * @author fuxinzhong
 * @date 2020/08/17
 */
@Component
public class MyParamsConverter implements Converter<String, Date> {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Date convert(String source) {
        System.err.println("MyParamsConverter is running");
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.err.println("MyParamsConverter result is parse:" + parse);

        return parse;
    }

    /**
     * HttpMessageConverter 只负责解析Http包的Body体部分，其余部分都交由相关的Convert SPI处理
     *
     * SpringMVC还有一些需要Convert SPI的场景，如读取Cookie值的@CookieValue（本质是Header），
     * 解析矩阵URL的@MatrixVariable（本质是URL），读取本地会话的@SessionAttribute，解析SpEL的@Value
     *
     *
     * 在SpringMVC处理请求时，HttpMessageConverter和Convert SPI分别用来反序列化请求的Body和非Body部分，即HttpMessageConverter是一套小型、独立、额外为用户提供的专门的Body体的类型转换器；
     * 而Convert SPI则与PropertyEditor类似，可以处理更为通用的类型转换。
     */

    /**
     * Spring MVC通过反射机制对目标处理方法签名进行分析，将请求消息绑定到处理方法入参中核心部件是DataBinder。数据绑定一般流程：
     *  1. 将ServletRequest对象及处理方法入参对象实例传给DateBinder。
     *
     *  2. DataBinder调用转配在Spring Web上下文中的ConversionService进行数据类型转换、数据格式化等工作，将ServletRequest中的消息填充到入参对象中。
     *
     *  3. 调用Validator对已经绑定的请求信息数据的入参对象进行数据合法性校验，生成数据绑定结果BindingResult。BindingResult包含完成绑定的入参对象和相应的校验错误对象。而后将BindingResult中的入参对象及校验错误对象赋给处理方法的入参。
     */
}
