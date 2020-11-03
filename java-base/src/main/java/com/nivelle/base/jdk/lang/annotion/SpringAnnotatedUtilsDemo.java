package com.nivelle.base.jdk.lang.annotion;

import com.nivelle.base.jdk.lang.annotion.myannotion.MyAnnotation;
import com.nivelle.base.jdk.lang.annotion.myannotion.UserLogin;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.MultiValueMap;

import java.util.Set;

/**
 * spring注解工具类
 *
 * @author fuxinzhong
 * @date 2020/08/02
 */
public class SpringAnnotatedUtilsDemo {
    public static void main(String[] args) {
        /**
         * 判断被注解元素是否被某个注解修饰
         */
        System.out.println(AnnotatedElementUtils.isAnnotated(UserLogin.class, MyAnnotation.class.getName()));

        /**
         * 在element上查询annotationType类型注解将查询出的多个annotationType类型注解属性合并到查询的第一个注解中多个相同注解合并
         */
        Set set = AnnotatedElementUtils.findAllMergedAnnotations(UserLogin.class, MyAnnotation.class);
        System.out.println("注解集合:" + set);

        /**
         * 获取注解属性值,第一个注解
         */
        MultiValueMap multiValueMap = AnnotatedElementUtils.getAllAnnotationAttributes(UserLogin.class, MyAnnotation.class.getName());
        System.out.println("multiValueMap:" + multiValueMap);

    }
}
