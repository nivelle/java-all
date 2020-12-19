package com.nivelle.base.jdk.jvm;

/**
 * 方法执行和栈帧
 *
 *
 *
 * @author fuxinzhong
 * @date 2020/12/19
 */
/**
 * 编译：javac com\nivelle\base\jdk\StackFrameDemo.java
 * 反编译：javap -p -v com\nivelle\base\jdk\StackFrameDemo.class
 */


/**
 * Classfile /Users/nivellefu/IdeaProjects/java-guides/java-base/src/main/java/com/nivelle/base/jdk/jvm/StackFrameDemo.class
 *   Last modified 2020年12月19日; size 386 bytes
 *   MD5 checksum cfc648f28ba9588e1691b86a093537b2
 *   Compiled from "StackFrameDemo.java"
 * public class com.nivelle.base.jdk.jvm.StackFrameDemo
 *   minor version: 0
 *   major version: 54
 *   flags: (0x0021) ACC_PUBLIC, ACC_SUPER  //方法类型：这里为私有的静态方法
 *   this_class: #3                          // com/nivelle/base/jdk/jvm/StackFrameDemo
 *   super_class: #4                         // java/lang/Object
 *   interfaces: 0, fields: 0, methods: 3, attributes: 1
 * Constant pool:
 *    #1 = Methodref          #4.#15         // java/lang/Object."<init>":()V
 *    #2 = Methodref          #3.#16         // com/nivelle/base/jdk/jvm/StackFrameDemo.add:(II)I
 *    #3 = Class              #17            // com/nivelle/base/jdk/jvm/StackFrameDemo
 *    #4 = Class              #18            // java/lang/Object
 *    #5 = Utf8               <init>
 *    #6 = Utf8               ()V
 *    #7 = Utf8               Code
 *    #8 = Utf8               LineNumberTable
 *    #9 = Utf8               main
 *   #10 = Utf8               ([Ljava/lang/String;)V
 *   #11 = Utf8               add
 *   #12 = Utf8               (II)I
 *   #13 = Utf8               SourceFile
 *   #14 = Utf8               StackFrameDemo.java
 *   #15 = NameAndType        #5:#6          // "<init>":()V
 *   #16 = NameAndType        #11:#12        // add:(II)I
 *   #17 = Utf8               com/nivelle/base/jdk/jvm/StackFrameDemo
 *   #18 = Utf8               java/lang/Object
 * {
 *   public com.nivelle.base.jdk.jvm.StackFrameDemo();
 *     descriptor: ()V
 *     flags: (0x0001) ACC_PUBLIC
 *     Code:
 *       stack=1, locals=1, args_size=1
 *          0: aload_0
 *          1: invokespecial #1                  // Method java/lang/Object."<init>":()V
 *          4: return
 *       LineNumberTable:
 *         line 9: 0
 *
 *   public static void main(java.lang.String[]);
 *     descriptor: ([Ljava/lang/String;)V //
 *     flags: (0x0009) ACC_PUBLIC, ACC_STATIC
 *     Code:
 *       stack=2, locals=1, args_size=1
 *          0: iconst_1
 *          1: iconst_2
 *          2: invokestatic  #2                  // Method add:(II)I
 *          5: pop
 *          6: return
 *       LineNumberTable:
 *         line 12: 0
 *         line 13: 6
 *
 *   private static int add(int, int);
 *     descriptor: (II)I //方法描述：括号内为入数类型，这里为两个int型入参，括号外为返回类型，这里返回类型为int型
 *     flags: (0x000a) ACC_PRIVATE, ACC_STATIC
 *     Code:
 *       stack=2, locals=3, args_size=2 //操作数栈为2,本地变量容量为3,入参个数为2
 *          0: iconst_0 //常量0压入操作数栈
 *          1: istore_2 //将栈顶出栈，即c=0，局部变量表多了个c=0
 *          2: iload_0 //复制a变量的值入栈
 *          3: iload_1 //复制b变量的值入栈
 *          4: iadd //将栈顶两个元素出栈，做加法，然后把结果再入栈（即a,b出栈，将a+b入栈）
 *          5: istore_2//将栈顶元素出栈，也就是a+b=3出栈，放到局部变量表c=3
 *          6: iload_2 //再将c=3 入栈
 *          7: ireturn //返回栈顶元素
 *       LineNumberTable: //LineNumberTable为代码行号与字节码行号的对应关系
 *         line 101: 0
 *         line 102: 2
 *         line 103: 6 //103 是原文件行号，6是程序计数器的程序执行索引
 * }
 * SourceFile: "StackFrameDemo.java"
 */
public class StackFrameDemo {

    public static void main(String[] args) {
        add(1,2);
    }

    private static int add(int a, int b) {
        int c = 0;
        c = a + b;
        return c;
    }
}

