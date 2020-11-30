package com.nivelle.spring.springcore.annotation;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * T
 *
 * @author nivelle
 * @date 2019/09/24
 */
public class MyCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String osName = context.getEnvironment().getProperty("os.name");
        System.out.println("OSNAME is" + osName + "from:" + metadata.toString());
        if (osName.contains("Mac")) {
            System.err.println("OSNAME is mac");
            return true;
        } else {
            return false;
        }
    }

}
