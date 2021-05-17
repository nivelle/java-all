package com.nivelle.core.jdk.util;

import java.io.*;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 布隆过滤器
 *
 * @author fuxinzhong
 * @date 2020/10/20
 */
public class MyBloomFilter implements Serializable {

    private final int[] seeds;
    private final int size;
    private final BitSet notebook;
    private final MisjudgmentRate rate;
    private final AtomicInteger useCount = new AtomicInteger();
    private final Double autoClearRate;

    //dataCount逾预期处理的数据规模
    public MyBloomFilter(int dataCount) {
        this(MisjudgmentRate.MIDDLE, dataCount, null);
    }

    //自动清空过滤器内部信息的使用比率，传null则表示不会自动清理;
    //当过滤器使用率达到100%时，则无论传入什么数据，都会认为在数据已经存在了;
    //当希望过滤器使用率达到80%时自动清空重新使用，则传入0.8
    public MyBloomFilter(MisjudgmentRate rate, int dataCount, Double autoClearRate) {
        //每个字符串需要的bit位数*总数据量
        long bitSize = rate.seeds.length * dataCount;
        if (bitSize < 0 || bitSize > Integer.MAX_VALUE) {
            throw new RuntimeException("位数太大溢出了，请降低误判率或者降低数据大小");
        }
        this.rate = rate;
        seeds = rate.seeds;
        size = (int) bitSize;
        //创建一个BitSet位集合
        notebook = new BitSet(size);
        this.autoClearRate = autoClearRate;
    }

    //如果存在返回true,不存在返回false
    public boolean addIfNotExist(String data) {
        //是否需要清理
        checkNeedClear();
        //seeds.length决定每一个string对应多少个bit位，每一位都有一个索引值
        //给定data，求出data字符串的第一个索引值index，如果第一个index值对应的bit=false说明，该data值不存在，则直接将所有对应bit位置为true即可;
        //如果第一个index值对应bit=true，则将index值保存，但此时并不能说明data已经存在，
        //则继续求解第二个index值，若所有index值都不存在则说明该data值不存在，将之前保存的index数组对应的bit位置为true
        int[] indexs = new int[seeds.length];
        //假定data已经存在
        boolean exist = true;
        int index;
        for (int i = 0; i < seeds.length; i++) {
            //计算位hash值
            indexs[i] = index = hash(data, seeds[i]);
            if (exist) {
                //如果某一位bit不存在，则说明该data不存在
                if (!notebook.get(index)) {
                    exist = false;
                    //将之前的bit位置为true
                    for (int j = 0; j <= i; j++) {
                        setTrue(indexs[j]);
                    }
                }
            } else {
                //如果不存在则直接置为true
                setTrue(index);
            }
        }

        return exist;
    }

    private int hash(String data, int seeds) {
        char[] value = data.toCharArray();
        int hash = 0;
        if (value.length > 0) {
            for (int i = 0; i < value.length; i++) {
                hash = i * hash + value[i];
            }
        }
        hash = hash * seeds % size;
        return Math.abs(hash);
    }

    private void setTrue(int index) {
        useCount.incrementAndGet();
        notebook.set(index, true);
    }

    //如果BitSet使用比率超过阈值，则将BitSet清零
    private void checkNeedClear() {
        if (autoClearRate != null) {
            if (getUseRate() >= autoClearRate) {
                synchronized (this) {
                    if (getUseRate() >= autoClearRate) {
                        notebook.clear();
                        useCount.set(0);
                    }
                }
            }
        }
    }

    private Double getUseRate() {
        return (double) useCount.intValue() / (double) size;
    }

    public void saveFilterToFile(String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MyBloomFilter readFilterFromFile(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (MyBloomFilter) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清空过滤器中的记录信息
     */
    public void clear() {
        useCount.set(0);
        notebook.clear();
    }

    public MisjudgmentRate getRate() {
        return rate;
    }

    /**
     * 分配的位数越多，误判率越低但是越占内存
     * <p>
     * 4个位误判率大概是0.14689159766308
     * <p>
     * 8个位误判率大概是0.02157714146322
     * <p>
     * 16个位误判率大概是0.00046557303372
     * <p>
     * 32个位误判率大概是0.00000021167340
     */
    public enum MisjudgmentRate {
        // 这里要选取质数，能很好的降低错误率
        /**
         * 每个字符串分配4个位
         */
        VERY_SMALL(new int[]{2, 3, 5, 7}),
        /**
         * 每个字符串分配8个位
         */
        SMALL(new int[]{2, 3, 5, 7, 11, 13, 17, 19}), //
        /**
         * 每个字符串分配16个位
         */
        MIDDLE(new int[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53}), //
        /**
         * 每个字符串分配32个位
         */
        HIGH(new int[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
                101, 103, 107, 109, 113, 127, 131});

        private int[] seeds;

        //枚举类型MIDDLE构造函数将seeds数组初始化
        private MisjudgmentRate(int[] seeds) {
            this.seeds = seeds;
        }

        public int[] getSeeds() {
            return seeds;
        }

        public void setSeeds(int[] seeds) {
            this.seeds = seeds;
        }
    }

    public static void main(String[] args) {
        MyBloomFilter fileter = new MyBloomFilter(7);
        System.out.println(fileter.addIfNotExist("1111111111111"));
        System.out.println(fileter.addIfNotExist("2222222222222222"));
        System.out.println(fileter.addIfNotExist("3333333333333333"));
        System.out.println(fileter.addIfNotExist("444444444444444"));
        System.out.println(fileter.addIfNotExist("5555555555555"));
        System.out.println(fileter.addIfNotExist("6666666666666"));
        System.out.println(fileter.addIfNotExist("1111111111111"));
        //fileter.saveFilterToFile("C:\\Users\\john\\Desktop\\1111\\11.obj");
        //fileter = readFilterFromFile("C:\\Users\\john\\Desktop\\111\\11.obj");
        System.out.println(fileter.getUseRate());
        System.out.println(fileter.addIfNotExist("1111111111111"));
    }

}
