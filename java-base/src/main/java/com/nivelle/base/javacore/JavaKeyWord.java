package com.nivelle.base.javacore;

import com.nivelle.base.pojo.Father;
import com.nivelle.base.pojo.Son;
import com.nivelle.base.pojo.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * java 关键字
 *
 * @author fuxinzhong
 * @date 2019/07/18
 */
public class JavaKeyWord {

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void main(String[] args) {
        //retry不是java的关键字
        retryTest();
        System.out.println("================================");
        instanceOfTest();
        System.out.println("================================");
        finalTest();
        continueTest();
    }

    /**
     * 1. retry：需要放在for，while，do...while的前面声明，变量只跟在break和continue后面。
     * <p>
     * 2. retry后面跟循环，标记这个循环的位置。我们可以在continue或者break后面加retry,表示要跳到这个循环
     * ，其中break表示要跳过这个标记的循环，continue表示从这个标记的循环继续执行。
     */
    private static void retryTest() {
        //retry:
        for (; ; ) {
            System.out.println("再一次来到这里");
            retry:
            for (int i = 0; i < 10; i++) {
                if (atomicInteger.incrementAndGet() > 14) {
                    return;
                }
                if (i == 5) {
                    continue retry;
                }
                if (i == 8) {
                    break retry;
                }
                System.out.println("当前数字:" + i + " atomicInteger is " + atomicInteger);
            }
        }

    }

    /**
     * 1. null是一种特殊类型,null 引用也可以转换为任意引用类型
     * 2. 在 JavaSE规范 中对 instanceof 运算符的规定就是：如果 obj 为 null，那么将返回 false。
     * 3. 通过 ClassCastException 异常来判断是否是其子类型
     */
    private static void instanceOfTest() {
        System.out.println("1. null 属性Object 类型:" + (null instanceof Object));
//        int i = 0; //编译不通过
//        System.out.println("i 属性 int 类型:" + (i instanceof Integer));
        Integer i = 0;
        System.out.println("2. i实例属于类的实例对象:" + (i instanceof Integer));

        ArrayList arrayList = new ArrayList();
        System.out.println("3. arrayList 属于接口List的实现类:" + (arrayList instanceof List));

        List list = new ArrayList();
        System.out.println("4. list是ArrayList的实例,属于List接口的实现类:" + (list instanceof List));

        Son son = new Son(1, "nivelle", 10);
        System.out.println("5. son是其父类的实现类:" + (son instanceof Father));

        String[] strings = new String[]{};
        System.out.println("6. 数组类型是否是 Object 的子类型:" + (strings instanceof Object));
        System.out.println("6.1. 数组类型是否是 String[] 的子类型:" + (strings instanceof String[]));
    }

    /**
     * final可以修饰变量,方法,方法,类
     */
    private static void finalTest() {

        final User user = new User(10, "jessy");
        //定义为final 的user引用指向的对象引用这个应用不可以改变，但是对象的内容是可以改变的
        // user = null;
        user.setAge(12);
        System.out.println("change user " + user);
        final int temp = 14;
        int result = changeInt(temp);
        System.out.println("值传递:" + result);
        System.out.println("参数不变:" + temp);
        //定义为final的变量不能改变
        //temp = 16;
        System.out.println("默认的byte值:"+0);
    }

    /**
     * 值传递,
     * 值传递（pass by value）:是指在调用函数时将实际参数复制一份传递到函数中，这样在函数中如果对参数进行修改，将不会影响到实际参数
     * 引用传递（pass by reference）:是指在调用函数时将实际参数的地址直接传递到函数中，那么在函数中对参数所进行的修改，将影响到实际参数。
     *
     * @param temp
     * @return
     */
    private static int changeInt(int temp) {
        temp++;
        return temp;
    }

    /**
     * continue :跳转到条件判断处
     */
    public static void continueTest() {
        int i = 0;
        while (i<=5) {
            if (i == 3) {
                i++;
                continue;
            }
            System.err.println(" i=" + i);
            i+=1;
        }
        System.err.println("end i=" + i);
    }


}
