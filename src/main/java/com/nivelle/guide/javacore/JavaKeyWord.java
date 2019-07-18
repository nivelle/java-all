package com.nivelle.guide.javacore;

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
        retryTest();
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
}
