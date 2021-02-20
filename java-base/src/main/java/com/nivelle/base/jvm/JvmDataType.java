package com.nivelle.base.jvm;


/**
 * jvm 数据类型
 *
 * @author fuxinzhong
 * @date 2021/02/08
 */
public class JvmDataType {
    static boolean boolValue;

    public static void main(String[] args) {
        //boolean 吃过饭没 = 2; // 直接编译的话javac会报错
        boolean 吃过饭没 = true;
        if (吃过饭没) {//jvm 编译成整数 其实相当于问：你不会一碗饭都没吃吧
            System.out.println("吃了");
        }
        if (true == 吃过饭没) { // 你吃过一碗饭了么
            System.out.println("真吃了");
        }

        int intType = new Byte("1");
        System.out.println(intType);

        boolValue = true;
        if (boolValue) {
            System.out.println("hello java");
        }
        if (boolValue == true) {
            System.out.println("hello java true");
        }

    }
}
