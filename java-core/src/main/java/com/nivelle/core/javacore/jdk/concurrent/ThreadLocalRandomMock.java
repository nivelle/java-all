package com.nivelle.core.javacore.jdk.concurrent;

import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 生产并发随机数
 *
 * @author fuxinzhong
 * @date 2020/11/03
 */
public class ThreadLocalRandomMock {

    public static void main(String[] args) {
        /**
         * 1. 我们不再有从多个线程访问同一个随机数生成器实例的争夺。
         *
         * 2. 取代以前每个随机变量实例化一个随机数生成器实例，我们可以每个线程实例化一个
         */
    }

    @Test(threadPoolSize = 5, invocationCount = 6)
    public void test1() {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        int randomInt1 = threadLocalRandom.nextInt(50);
        System.out.println("randomInt1:" + randomInt1);//随机生成0~50的随机数，不包括50
        int randomInt2 = threadLocalRandom.nextInt(30, 50);
        System.out.println("randomInt2:" + randomInt2);//随机生成30~50的随机数，不包括50
    }

    /**
     * 通过相同的种子,产生的随机数是相同的
     */
    @Test(threadPoolSize = 5, invocationCount = 6)
    public void test2() {
        Random random = new Random(2);
        int randomInt1 = random.nextInt(50);
        System.out.println("randomInt1:" + randomInt1);//随机生成0~50的随机数，不包括50
        int randomInt2 = random.nextInt(30) + 50;
        System.out.println("randomInt2:" + randomInt2);//随机生成30~50的随机数，不包括50
    }
}
