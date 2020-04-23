package com.nivelle.spring.springcore.annotation;

import org.springframework.core.type.AnnotationMetadata;

/**
 * 自定义bean注册器
 *
 * @author nivell
 * @date 2019/09/24
 */
public class ImportSelector implements org.springframework.context.annotation.ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        //要注入类的全限定名
        return new String[]{"com.nivelle.spring.pojo.Cat"};
    }
}
