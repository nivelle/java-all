package com.nivelle.base.jdk.key;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.testng.annotations.Test;

/**
 * 布隆过滤
 *
 * @author fuxinzhong
 * @date 2020/10/28
 */
public class GoogleBloomFilter {
    private static int SIZE = 1000000;//预计要插入多少数据

    private static double FPP = 0.001;//期望的误判率

    private static BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), SIZE, FPP);

    /**
     * 布隆过滤器判断某个元素存在，小概率会误判。
     * 也就是说布隆过滤器说某个元素不在，那么这个元素一定不在；布隆过滤器说某个元素存在，这个元素可能不存在
     */
    @Test
    public void googleBloomFilterTest() {
        //插入数据
        for (int i = 0; i < 1000000; i++) {
            bloomFilter.put(i);
        }
        int count = 0;
        for (int i = 0; i < 2000000; i++) {
            if (bloomFilter.mightContain(i)) {
                count++;
                //System.out.println(i + "误判了");
            }
        }
        System.out.println("总共的误判数:" + count);
    }
}
