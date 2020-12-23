package com.nivelle.rpc.dubbo.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * MVC配置
 *
 * @author nivelle
 * @date 2019/08/19
 */
@Configuration
public class MyJacksonAutoConfiguration implements WebMvcConfigurer {


    //修改已经注册的转换器
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        FastJsonHttpMessageConverter fjc = new FastJsonHttpMessageConverter();
        FastJsonConfig fj = new FastJsonConfig();
        //设置value的空字符串为""而不是null
        fj.setSerializerFeatures(SerializerFeature.WriteNullStringAsEmpty);
        fjc.setFastJsonConfig(fj);
        converters.add(0, fjc);
    }
}
