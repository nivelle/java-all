package com.nivelle.spring.springmvc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 自定义类型转换
 *
 * @author fuxinzhong
 * @date 2020/08/17
 */
@Component
public class MyParamsConverter implements Converter<String, Date> {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Date convert(String source) {
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parse;
    }
}
