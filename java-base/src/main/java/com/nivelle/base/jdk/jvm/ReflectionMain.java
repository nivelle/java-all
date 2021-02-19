package com.nivelle.base.jdk.jvm;

import java.lang.reflect.Method;

/**
 * 反射反编译指令
 *
 * @author nivelle
 * @date 2020/03/28
 */
public class ReflectionMain {


    public void reflection() throws Exception {
        String methodName = "length";
        Method method = String.class.getDeclaredMethod(methodName);
        Object result = method.invoke("abc");
        System.out.println(result);
    }
}
/**
 * nivelleMac:jvm nivellefu$ javap -v ReflectionMain
 * 警告: 文件 ./ReflectionMain.class 不包含类 ReflectionMain
 * Classfile /Users/nivellefu/IdeaProjects/javadaybyday/java-base/src/main/java/com/nivelle/base/javacore/jvm/com/nivelle/base/javacore/jvm/ReflectionMain.class
 * Last modified 2020年3月28日; size 781 bytes
 * MD5 checksum b595739d187eb2b0eb7bbee15e63fd67
 * Compiled from "ReflectionMain.java"
 * public class com.nivelle.base.javacore.jvm.ReflectionMain
 * minor version: 0
 * major version: 54
 * flags: (0x0021) ACC_PUBLIC, ACC_SUPER
 * this_class: #11                         // com/nivelle/base/javacore/jvm/ReflectionMain
 * super_class: #7                         // java/lang/Object
 * interfaces: 0, fields: 0, methods: 2, attributes: 1
 * Constant pool:
 * #1 = Methodref          #7.#21         // java/lang/Object."<init>":()V
 * #2 = String             #22            // length
 * #3 = Class              #23            // java/lang/String
 * #4 = Class              #24            // java/lang/Class
 * #5 = Methodref          #4.#25         // java/lang/Class.getDeclaredMethod:(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
 * #6 = String             #26            // abc
 * #7 = Class              #27            // java/lang/Object
 * #8 = Methodref          #28.#29        // java/lang/reflect/Method.invoke:(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
 * #9 = Fieldref           #30.#31        // java/lang/System.out:Ljava/io/PrintStream;
 * #10 = Methodref          #32.#33        // java/io/PrintStream.println:(Ljava/lang/Object;)V
 * #11 = Class              #34            // com/nivelle/base/javacore/jvm/ReflectionMain
 * #12 = Utf8               <init>
 * #13 = Utf8               ()V
 * #14 = Utf8               Code
 * #15 = Utf8               LineNumberTable
 * #16 = Utf8               reflection
 * #17 = Utf8               Exceptions
 * #18 = Class              #35            // java/lang/Exception
 * #19 = Utf8               SourceFile
 * #20 = Utf8               ReflectionMain.java
 * #21 = NameAndType        #12:#13        // "<init>":()V
 * #22 = Utf8               length
 * #23 = Utf8               java/lang/String
 * #24 = Utf8               java/lang/Class
 * #25 = NameAndType        #36:#37        // getDeclaredMethod:(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
 * #26 = Utf8               abc
 * #27 = Utf8               java/lang/Object
 * #28 = Class              #38            // java/lang/reflect/Method
 * #29 = NameAndType        #39:#40        // invoke:(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
 * #30 = Class              #41            // java/lang/System
 * #31 = NameAndType        #42:#43        // out:Ljava/io/PrintStream;
 * #32 = Class              #44            // java/io/PrintStream
 * #33 = NameAndType        #45:#46        // println:(Ljava/lang/Object;)V
 * #34 = Utf8               com/nivelle/base/javacore/jvm/ReflectionMain
 * #35 = Utf8               java/lang/Exception
 * #36 = Utf8               getDeclaredMethod
 * #37 = Utf8               (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
 * #38 = Utf8               java/lang/reflect/Method
 * #39 = Utf8               invoke
 * #40 = Utf8               (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
 * #41 = Utf8               java/lang/System
 * #42 = Utf8               out
 * #43 = Utf8               Ljava/io/PrintStream;
 * #44 = Utf8               java/io/PrintStream
 * #45 = Utf8               println
 * #46 = Utf8               (Ljava/lang/Object;)V
 * {
 * public com.nivelle.base.javacore.jvm.ReflectionMain();
 * descriptor: ()V
 * flags: (0x0001) ACC_PUBLIC
 * Code:
 * stack=1, locals=1, args_size=1
 * 0: aload_0
 * 1: invokespecial #1                  // Method java/lang/Object."<init>":()V
 * 4: return
 * LineNumberTable:
 * line 11: 0
 * <p>
 * public void reflection() throws java.lang.Exception;
 * descriptor: ()V
 * flags: (0x0001) ACC_PUBLIC
 * Code:
 * stack=3, locals=4, args_size=1
 * 0: ldc           #2                  // String length
 * 2: astore_1
 * 3: ldc           #3                  // class java/lang/String
 * 5: aload_1
 * 6: iconst_0
 * 7: anewarray     #4                  // class java/lang/Class
 * 10: invokevirtual #5                  // Method java/lang/Class.getDeclaredMethod:(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
 * 13: astore_2
 * 14: aload_2
 * 15: ldc           #6                  // String abc
 * 17: iconst_0
 * 18: anewarray     #7                  // class java/lang/Object
 * 21: invokevirtual #8                  // Method java/lang/reflect/Method.invoke:(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
 * 24: astore_3
 * 25: getstatic     #9                  // Field java/lang/System.out:Ljava/io/PrintStream;
 * 28: aload_3
 * 29: invokevirtual #10                 // Method java/io/PrintStream.println:(Ljava/lang/Object;)V
 * 32: return
 * LineNumberTable:
 * line 15: 0
 * line 16: 3
 * line 17: 14
 * line 18: 25
 * line 19: 32
 * Exceptions:
 * throws java.lang.Exception
 * }
 * SourceFile: "ReflectionMain.java"
 */
