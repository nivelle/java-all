package com.nivelle.spring.springmvc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;

/**
 * 类型转换器注册
 *
 * @author fuxinzhong
 * @date 2020/08/18
 */
public class MyConverterRegistry implements ConverterRegistry {
    @Override
    public void addConverter(Converter<?, ?> converter) {

    }

    @Override
    public <S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter) {

    }

    @Override
    public void addConverter(GenericConverter converter) {

    }

    @Override
    public void addConverterFactory(ConverterFactory<?, ?> factory) {

    }

    @Override
    public void removeConvertible(Class<?> sourceType, Class<?> targetType) {

    }
}
