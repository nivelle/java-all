package com.nivelle.spring.springcore.annotation;

import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;


/**
 * 自定义扫描过滤条件
 *
 * @author nivelle
 * @date 2019/09/23
 */
public class MyTypeFilter implements TypeFilter {

    private ClassMetadata classMetadata;

    /**
     * MetadataReader:读取到当前正在扫描类的信息
     * MetadataReaderFactory:可以获取到其他任何类信息
     */
    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        //获取当前类注解的信息
        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
        //获取当前正在扫描的类信息
        classMetadata = metadataReader.getClassMetadata();
        //获取当前类资源(类的路径)
        Resource resource = metadataReader.getResource();
        String className = classMetadata.getClassName();
        //System.out.println(annotationMetadata.getAnnotatedMethods("Bean"));
        System.out.println("过滤不扫描的类----->" + className);
        if (className.equals("com.nivelle.spring.configbean.MyScanConfig")) {
            return true;
        }
        return false;
    }
}

