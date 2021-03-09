package com.nivelle.agent;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * 使用javassist来动态替换类
 *
 * @author fuxinzhong
 * @date 2020/11/27
 */
public class MyClassTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {
        // 操作Date类
        if ("java/util/Date".equals(className)) {
            try {
                // 从ClassPool获得CtClass对象
                final ClassPool classPool = ClassPool.getDefault();
                final CtClass clazz = classPool.get("java.util.Date");
                CtMethod convertToAbbr = clazz.getDeclaredMethod("convertToAbbr");
                //这里对 java.util.Date.convertToAbbr() 方法进行了改写，在 return之前增加了一个 打印操作
                String methodBody = "{sb.append(Character.toUpperCase(name.charAt(0)));" +
                        "sb.append(name.charAt(1)).append(name.charAt(2));" +
                        "System.out.println(\"sb.toString()\");" +
                        "return sb;}";
                convertToAbbr.setBody(methodBody);

                // 返回字节码，并且detachCtClass对象
                byte[] byteCode = clazz.toBytecode();
                //detach的意思是将内存中曾经被javassist加载过的Date对象移除，如果下次有需要在内存中找不到会重新走javassist加载
                clazz.detach();
                return byteCode;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // 如果返回null则字节码不会被修改
        return null;
    }

    /**
     * 使用 javaagent 需要几个步骤：
     *
     * 1. 定义一个 MANIFEST.MF 文件，必须包含 Premain-Class 选项，通常也会加入Can-Redefine-Classes 和 Can-Retransform-Classes 选项。
     * 2. 创建一个Premain-Class 指定的类，类中包含 premain 方法，方法逻辑由用户自己确定。
     * 3. 将 premain 的类和 MANIFEST.MF 文件打成 jar 包。
     * 4. 使用参数 -javaagent: jar包路径 启动要代理的方法。
     *
     * 在执行以上步骤后，JVM 会先执行 premain 方法，大部分类加载都会通过该方法，注意：是大部分，不是所有。当然，遗漏的主要是系统类，因为很多系统类先于 agent 执行，而用户类的加载肯定是会被拦截的。
     * 也就是说，这个方法是在 main 方法启动前拦截大部分类的加载活动，既然可以拦截类的加载，那么就可以去做重写类这样的操作，结合第三方的字节码编译工具，比如ASM，javassist，cglib等等来改写实现类。
     *
     * 通过上面的步骤我们用代码实现来实现。实现 javaagent 你需要搭建两个工程，一个工程是用来承载 javaagent类，单独的打成jar包；一个工程是javaagent需要去代理的类。即javaagent会在这个工程中的main方法启动之前去做一些事情。
     */
}
