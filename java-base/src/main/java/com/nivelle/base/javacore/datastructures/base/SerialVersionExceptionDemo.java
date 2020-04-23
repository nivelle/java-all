package com.nivelle.base.javacore.datastructures.base;

import java.io.*;

/**
 * java本身序列化和反序列化会校验类 serialVersionUID 是否匹配，不同系统不同JVM默认生成的serialVersionUID可能不一致，保证编译路径一致。
 *
 * @author nivell
 * @date 2020/04/02
 */
public class SerialVersionExceptionDemo implements Serializable {
    /**
     * Exception in thread "main" java.io.InvalidClassException: com.nivelle.base.bugs.SerialVersionException;
     * local class incompatible: stream classdesc serialVersionUID = 1, local class serialVersionUID = 2
     */
    /**
     * 序列化运行时将每个可序列化类与版本号相关联，称为serialVersionUID,
     *
     * 在反序列化期间使用该版本号来验证序列化对象的发送方和接收方是否已加载与该序列化兼容的该对象的类。
     * 如果接收者为具有与相应发送者类的serialVersionUID不同的对象加载了一个类，则反序列化将导致InvalidClassException
     *
     */

    /**
     * serialVersionUID两种生成方式：
     *  1.显式声明，该字段必须是static,final和long类型:private static final long serialVersionUID = 1L;
     * 2.如果没有显式声明serialVersionUID,JVM将使用自己的算法生成默认SerialVersionUID。
     * <p>
     * 强烈建议所有可序列化类显式声明serialVersionUID值，因为默认的serialVersionUID计算对类细节高度敏感，这些细节可能因编译器实现而异，因此在反序列化期间可能导致意外的InvalidClassExceptions。因此，为了保证跨不同java编译器实现的一致的serialVersionUID值，可序列化类必须声明显式的serialVersionUID值。
     */
    private static final long serialVersionUID = 2L;


    public static void main(String[] args) throws Exception {
        SerialVersionExceptionDemo serialVersionException = new SerialVersionExceptionDemo();
        // 写对象
        ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream("serialVersionException.bin"));
        output.writeObject(serialVersionException);
        output.close();

        // 读取对象
        ObjectInputStream input = new ObjectInputStream(new FileInputStream("constructor5.bin"));
        SerialVersionExceptionDemo serialVersionExceptionFile = (SerialVersionExceptionDemo) input.readObject();
        System.out.println(serialVersionExceptionFile);
    }


}
