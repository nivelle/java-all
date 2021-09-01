package com.nivelle.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * 在main方法执行之前做的一些操作
 *
 * @author fuxinzhong
 * @date 2020/11/26
 */
public class PreMainTraceAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("java-agent agentArgs : " + agentArgs);
        inst.addTransformer(new DefineTransformer(), true);
    }

    static class DefineTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFiledBuffer) throws IllegalClassFormatException {
            System.out.println("java-agent load Class:" + className);
            return classFiledBuffer;
        }
    }
}
