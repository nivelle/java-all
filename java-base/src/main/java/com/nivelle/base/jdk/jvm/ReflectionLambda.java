package com.nivelle.base.jdk.jvm;

/**
 * 反射lambad表达式原理
 *
 * @author nivelle
 * @date 2020/03/29
 */
public class ReflectionLambda {

    public void lambda(ReflectionFunc reflectionFunc) {
        return;
    }

    public static void main(String[] args) {
        ReflectionLambda lambda = new ReflectionLambda();
        lambda.lambda(s -> {
            return true;
        });
        lambda.lambda(s -> {
            return true;
        });
    }
}
/**
 * Classfile /Users/nivellefu/IdeaProjects/javadaybyday/java-base/src/main/java/com/nivelle/base/javacore/jvm/com/nivelle/base/javacore/jvm/ReflectionLambda.class
 * Last modified 2020年3月29日; size 1141 bytes
 * MD5 checksum 5c06346167e356f617c7a7d72d1b8480
 * Compiled from "ReflectionLambda.java"
 * public class com.nivelle.base.javacore.jvm.ReflectionLambda
 * minor version: 0
 * major version: 54
 * flags: (0x0021) ACC_PUBLIC, ACC_SUPER
 * this_class: #2                          // com/nivelle/base/javacore/jvm/ReflectionLambda
 * super_class: #7                         // java/lang/Object
 * interfaces: 0, fields: 0, methods: 5, attributes: 3
 * Constant pool:
 * #1 = Methodref          #7.#21         // java/lang/Object."<init>":()V
 * #2 = Class              #22            // com/nivelle/base/javacore/jvm/ReflectionLambda
 * #3 = Methodref          #2.#21         // com/nivelle/base/javacore/jvm/ReflectionLambda."<init>":()V
 * #4 = InvokeDynamic      #0:#27         // #0:func:()Lcom/nivelle/base/javacore/jvm/ReflectionFunc;
 * #5 = Methodref          #2.#28         // com/nivelle/base/javacore/jvm/ReflectionLambda.lambda:(Lcom/nivelle/base/javacore/jvm/ReflectionFunc;)V
 * #6 = InvokeDynamic      #1:#27         // #1:func:()Lcom/nivelle/base/javacore/jvm/ReflectionFunc;
 * #7 = Class              #30            // java/lang/Object
 * #8 = Utf8               <init>
 * #9 = Utf8               ()V
 * #10 = Utf8               Code
 * #11 = Utf8               LineNumberTable
 * #12 = Utf8               lambda
 * #13 = Utf8               (Lcom/nivelle/base/javacore/jvm/ReflectionFunc;)V
 * #14 = Utf8               main
 * #15 = Utf8               ([Ljava/lang/String;)V
 * #16 = Utf8               lambda$main$1
 * #17 = Utf8               (Ljava/lang/String;)Z
 * #18 = Utf8               lambda$main$0
 * #19 = Utf8               SourceFile
 * #20 = Utf8               ReflectionLambda.java
 * #21 = NameAndType        #8:#9          // "<init>":()V
 * #22 = Utf8               com/nivelle/base/javacore/jvm/ReflectionLambda
 * #23 = Utf8               BootstrapMethods
 * #24 = MethodHandle       6:#31          // REF_invokeStatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
 * #25 = MethodType         #17            //  (Ljava/lang/String;)Z
 * #26 = MethodHandle       6:#32          // REF_invokeStatic com/nivelle/base/javacore/jvm/ReflectionLambda.lambda$main$0:(Ljava/lang/String;)Z
 * #27 = NameAndType        #33:#34        // func:()Lcom/nivelle/base/javacore/jvm/ReflectionFunc;
 * #28 = NameAndType        #12:#13        // lambda:(Lcom/nivelle/base/javacore/jvm/ReflectionFunc;)V
 * #29 = MethodHandle       6:#35          // REF_invokeStatic com/nivelle/base/javacore/jvm/ReflectionLambda.lambda$main$1:(Ljava/lang/String;)Z
 * #30 = Utf8               java/lang/Object
 * #31 = Methodref          #36.#37        // java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
 * #32 = Methodref          #2.#38         // com/nivelle/base/javacore/jvm/ReflectionLambda.lambda$main$0:(Ljava/lang/String;)Z
 * #33 = Utf8               func
 * #34 = Utf8               ()Lcom/nivelle/base/javacore/jvm/ReflectionFunc;
 * #35 = Methodref          #2.#39         // com/nivelle/base/javacore/jvm/ReflectionLambda.lambda$main$1:(Ljava/lang/String;)Z
 * #36 = Class              #40            // java/lang/invoke/LambdaMetafactory
 * #37 = NameAndType        #41:#45        // metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
 * #38 = NameAndType        #18:#17        // lambda$main$0:(Ljava/lang/String;)Z
 * #39 = NameAndType        #16:#17        // lambda$main$1:(Ljava/lang/String;)Z
 * #40 = Utf8               java/lang/invoke/LambdaMetafactory
 * #41 = Utf8               metafactory
 * #42 = Class              #47            // java/lang/invoke/MethodHandles$Lookup
 * #43 = Utf8               Lookup
 * #44 = Utf8               InnerClasses
 * #45 = Utf8               (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
 * #46 = Class              #48            // java/lang/invoke/MethodHandles
 * #47 = Utf8               java/lang/invoke/MethodHandles$Lookup
 * #48 = Utf8               java/lang/invoke/MethodHandles
 * {
 * public com.nivelle.base.javacore.jvm.ReflectionLambda();
 * descriptor: ()V
 * flags: (0x0001) ACC_PUBLIC
 * Code:
 * stack=1, locals=1, args_size=1
 * 0: aload_0
 * 1: invokespecial #1                  // Method java/lang/Object."<init>":()V
 * 4: return
 * LineNumberTable:
 * line 9: 0
 * <p>
 * public void lambda(com.nivelle.base.javacore.jvm.ReflectionFunc);
 * descriptor: (Lcom/nivelle/base/javacore/jvm/ReflectionFunc;)V
 * flags: (0x0001) ACC_PUBLIC
 * Code:
 * stack=0, locals=2, args_size=2
 * 0: return
 * LineNumberTable:
 * line 12: 0
 * <p>
 * public static void main(java.lang.String[]);
 * descriptor: ([Ljava/lang/String;)V
 * flags: (0x0009) ACC_PUBLIC, ACC_STATIC
 * Code:
 * stack=2, locals=2, args_size=1
 * 0: new           #2                  // class com/nivelle/base/javacore/jvm/ReflectionLambda
 * 3: dup
 * 4: invokespecial #3                  // Method "<init>":()V
 * 7: astore_1
 * 8: aload_1
 * 9: invokedynamic #4,  0              // InvokeDynamic #0:func:()Lcom/nivelle/base/javacore/jvm/ReflectionFunc;
 * 14: invokevirtual #5                  // Method lambda:(Lcom/nivelle/base/javacore/jvm/ReflectionFunc;)V
 * 17: aload_1
 * 18: invokedynamic #6,  0              // InvokeDynamic #1:func:()Lcom/nivelle/base/javacore/jvm/ReflectionFunc;
 * 23: invokevirtual #5                  // Method lambda:(Lcom/nivelle/base/javacore/jvm/ReflectionFunc;)V
 * 26: return
 * LineNumberTable:
 * line 15: 0
 * line 16: 8
 * line 17: 17
 * line 18: 26
 * }
 * SourceFile: "ReflectionLambda.java"
 * InnerClasses:
 * public static final #43= #42 of #46;    // Lookup=class java/lang/invoke/MethodHandles$Lookup of class java/lang/invoke/MethodHandles
 * BootstrapMethods:
 * 0: #24 REF_invokeStatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
 * Method arguments:
 * #25 (Ljava/lang/String;)Z
 * #26 REF_invokeStatic com/nivelle/base/javacore/jvm/ReflectionLambda.lambda$main$0:(Ljava/lang/String;)Z
 * #25 (Ljava/lang/String;)Z
 * 1: #24 REF_invokeStatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
 * Method arguments:
 * #25 (Ljava/lang/String;)Z
 * #29 REF_invokeStatic com/nivelle/base/javacore/jvm/ReflectionLambda.lambda$main$1:(Ljava/lang/String;)Z
 * #25 (Ljava/lang/String;)Z
 **/