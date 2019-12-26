package com.nivelle.rpc.core;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 自定义bean注册器
 *
 * @author fuxinzhong
 * @date 2019/09/24
 */
public class MyImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata){
        //要注入类的全限定名
       return new String[]{"com.nivelle.rpc.model.Cat"};
    }
}
