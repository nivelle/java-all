package com.nivelle.base.jdk.jvm;

/**
 * javap 命令学习,转换成汇编代码
 *
 * @author nivelle
 * @date 2020/03/25
 */
public class JavaCommonInvokeInstruction {

    public void invoke(){

        InvokeInterface sample = new InvokeInterfaceImpl();
        sample.invokeInterface();
        InvokeInterfaceImpl sampleImpl = new InvokeInterfaceImpl();
        sampleImpl.invokeNormalMethod();
        InvokeInterfaceImpl.invokeStaticMethod();
    }
}

/**
 *
 *
 * Classfile /Users/nivellefu/IdeaProjects/javadaybyday/java-base/target/classes/com/nivelle/base/javacore/jvm/JavaCommonInvokeInstruction.class
 *   Last modified 2020年3月26日; size 828 bytes
 *   MD5 checksum 0065f76f60c11113b68559077a712a3f
 *   Compiled from "JavaCommonInvokeInstruction.java"
 * public class com.nivelle.base.javacore.jvm.JavaCommonInvokeInstruction
 *   minor version: 0
 *   major version: 52
 *   flags: (0x0021) ACC_PUBLIC, ACC_SUPER
 *   this_class: #7                          // com/nivelle/base/javacore/jvm/JavaCommonInvokeInstruction
 *   super_class: #8                         // java/lang/Object
 *   interfaces: 0, fields: 0, methods: 2, attributes: 1
 * Constant pool:
 *    #1 = Methodref          #8.#23         // java/lang/Object."<init>":()V
 *    #2 = Class              #24            // com/nivelle/base/javacore/jvm/InvokeInterfaceImpl
 *    #3 = Methodref          #2.#23         // com/nivelle/base/javacore/jvm/InvokeInterfaceImpl."<init>":()V
 *    #4 = InterfaceMethodref #25.#26        // com/nivelle/base/javacore/jvm/InvokeInterface.invokeInterface:()V
 *    #5 = Methodref          #2.#27         // com/nivelle/base/javacore/jvm/InvokeInterfaceImpl.invokeNormalMethod:()V
 *    #6 = Methodref          #2.#28         // com/nivelle/base/javacore/jvm/InvokeInterfaceImpl.invokeStaticMethod:()V
 *    #7 = Class              #29            // com/nivelle/base/javacore/jvm/JavaCommonInvokeInstruction
 *    #8 = Class              #30            // java/lang/Object
 *    #9 = Utf8               <init>
 *   #10 = Utf8               ()V
 *   #11 = Utf8               Code
 *   #12 = Utf8               LineNumberTable
 *   #13 = Utf8               LocalVariableTable
 *   #14 = Utf8               this
 *   #15 = Utf8               Lcom/nivelle/base/javacore/jvm/JavaCommonInvokeInstruction;
 *   #16 = Utf8               invoke
 *   #17 = Utf8               sample
 *   #18 = Utf8               Lcom/nivelle/base/javacore/jvm/InvokeInterface;
 *   #19 = Utf8               sampleImpl
 *   #20 = Utf8               Lcom/nivelle/base/javacore/jvm/InvokeInterfaceImpl;
 *   #21 = Utf8               SourceFile
 *   #22 = Utf8               JavaCommonInvokeInstruction.java
 *   #23 = NameAndType        #9:#10         // "<init>":()V
 *   #24 = Utf8               com/nivelle/base/javacore/jvm/InvokeInterfaceImpl
 *   #25 = Class              #31            // com/nivelle/base/javacore/jvm/InvokeInterface
 *   #26 = NameAndType        #32:#10        // invokeInterface:()V
 *   #27 = NameAndType        #33:#10        // invokeNormalMethod:()V
 *   #28 = NameAndType        #34:#10        // invokeStaticMethod:()V
 *   #29 = Utf8               com/nivelle/base/javacore/jvm/JavaCommonInvokeInstruction
 *   #30 = Utf8               java/lang/Object
 *   #31 = Utf8               com/nivelle/base/javacore/jvm/InvokeInterface
 *   #32 = Utf8               invokeInterface
 *   #33 = Utf8               invokeNormalMethod
 *   #34 = Utf8               invokeStaticMethod
 * {
 *   public com.nivelle.base.javacore.jvm.JavaCommonInvokeInstruction();
 *     descriptor: ()V
 *     flags: (0x0001) ACC_PUBLIC
 *     Code:
 *       stack=1, locals=1, args_size=1
 *          0: aload_0
 *          1: invokespecial #1                  // Method java/lang/Object."<init>":()V
 *          4: return
 *       LineNumberTable:
 *         line 9: 0
 *       LocalVariableTable:
 *         Start  Length  Slot  Name   Signature
 *             0       5     0  this   Lcom/nivelle/base/javacore/jvm/JavaCommonInvokeInstruction;
 *
 *   public void invoke();
 *     descriptor: ()V
 *     flags: (0x0001) ACC_PUBLIC
 *     Code:
 *       stack=2, locals=3, args_size=1
 *          0: new           #2                  // class com/nivelle/base/javacore/jvm/InvokeInterfaceImpl
 *          3: dup
 *          4: invokespecial #3                  // Method com/nivelle/base/javacore/jvm/InvokeInterfaceImpl."<init>":()V
 *          7: astore_1
 *          8: aload_1
 *          9: invokeinterface #4,  1            // InterfaceMethod com/nivelle/base/javacore/jvm/InvokeInterface.invokeInterface:()V
 *         14: new           #2                  // class com/nivelle/base/javacore/jvm/InvokeInterfaceImpl
 *         17: dup
 *         18: invokespecial #3                  // Method com/nivelle/base/javacore/jvm/InvokeInterfaceImpl."<init>":()V
 *         21: astore_2
 *         22: aload_2
 *         23: invokevirtual #5                  // Method com/nivelle/base/javacore/jvm/InvokeInterfaceImpl.invokeNormalMethod:()V
 *         26: invokestatic  #6                  // Method com/nivelle/base/javacore/jvm/InvokeInterfaceImpl.invokeStaticMethod:()V
 *         29: return
 *       LineNumberTable:
 *         line 13: 0
 *         line 14: 8
 *         line 15: 14
 *         line 16: 22
 *         line 17: 26
 *         line 18: 29
 *       LocalVariableTable:
 *         Start  Length  Slot  Name   Signature
 *             0      30     0  this   Lcom/nivelle/base/javacore/jvm/JavaCommonInvokeInstruction;
 *             8      22     1 sample   Lcom/nivelle/base/javacore/jvm/InvokeInterface;
 *            22       8     2 sampleImpl   Lcom/nivelle/base/javacore/jvm/InvokeInterfaceImpl;
 * }
 * SourceFile: "JavaCommonInvokeInstruction.java"
 *
 *
 */
