package com.nivelle.spring.springmvc;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.GenericConversionService;
/**
 * 在自己的ConversionService里面注入一个GenericConversionService，然后通过自己的ConversionService的属性接收Converter并把它们注入到GenericConversionService中，之后所有关于ConversionService的方法逻辑都可以调用GenericConversionService对应的逻辑。
 *
 * @author fuxinzhong
 * @date 2021/04/14
 */
public class MyConversionService implements ConversionService {

    @Autowired
    private GenericConversionService conversionService;
    private Set<?> converters;

    /**
     * 通过converters属性我们可以接收需要注册的Converter、ConverterFactory和GenericConverter，
     * 在converters属性设置完成之后afterPropertiesSet方法会被调用，
     * 在这个方法里面我们把接收到的converters都注册到注入的GenericConversionService中了，
     * 之后关于ConversionService的其他操作都是通过这个GenericConversionService来完成的
     */
    @PostConstruct
    public void afterPropertiesSet() {
        if (converters != null) {
            for (Object converter : converters) {
                if (converter instanceof Converter<?, ?>) {
                    conversionService.addConverter((Converter<?, ?>) converter);
                } else if (converter instanceof ConverterFactory<?, ?>) {
                    conversionService.addConverterFactory((ConverterFactory<?, ?>) converter);
                } else if (converter instanceof GenericConverter) {
                    conversionService.addConverter((GenericConverter) converter);
                }
            }
        }
    }

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }

    @Override
    public boolean canConvert(TypeDescriptor sourceType,
                              TypeDescriptor targetType) {
        return conversionService.canConvert(sourceType, targetType);
    }

    @Override
    public <T> T convert(Object source, Class<T> targetType) {
        return conversionService.convert(source, targetType);
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType,
                          TypeDescriptor targetType) {
        return conversionService.convert(source, sourceType, targetType);
    }

    public Set<?> getConverters() {
        return converters;
    }

    public void setConverters(Set<?> converters) {
        this.converters = converters;
    }

}
