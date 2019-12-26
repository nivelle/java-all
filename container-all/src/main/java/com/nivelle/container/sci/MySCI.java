package com.nivelle.container.sci;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * 自定义SCI
 *
 * @author fuxinzhong
 * @date 2019/12/25
 */
@HandlesTypes(MyContainerInitalizer.class)
public class MySCI implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        for (Class<?> clazz : c) {
            if (!clazz.isInterface()) {
                try {
                    System.out.println(clazz);
                    Constructor<?> constructor = clazz.getConstructor();
                    Object instance = constructor.newInstance();
                    MyContainerInitalizer containerInitalizer = (MyContainerInitalizer) instance;
                    containerInitalizer.onStartup(ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(">>>>>>");
    }
}
