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
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined,final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {
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
}
