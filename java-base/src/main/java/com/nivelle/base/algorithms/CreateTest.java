package com.nivelle.base.algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CreateTest {
    private static List<Integer> findNumIndex;// 存储符合条件的数组元素下表  
    private static boolean findon;// 是否可以从数组中找到相加等于“和”的元素  
    private static boolean someoneEqualSum;// 数组中是否有某个元素等于“和”本身  
    private static double[] a;// 数组  
    private static double sum;// “和”  

    public CreateTest(double[] a, double sum) {
        CreateTest.a = a;
        CreateTest.sum = sum;
        findon = false;
        findNumIndex = new ArrayList<Integer>();
        someoneEqualSum = false;
    }

    public void start() {
        a = maopao(a);// 首先冒泡排序  
        List<Double> list = new LinkedList<Double>();
        for (int i = 0; i < a.length; i++) {// 把double数组付给list  
            list.add(a[i]);
        }
        boolean flag = true;
        do {
            double mix = list.get(0);// 当前最小值  
            double max = list.get(list.size() - 1);// 当前最大值  
            if (max == sum) {// 找到等于“和”的元素,打个标记  
                someoneEqualSum = true;
            }
            if (mix + max > sum && flag) {// 删除没用的最大值  
                list.remove(list.size() - 1);
            } else {
                flag = false;
            }
        } while (flag);
        startMath(list, sum);
        if (!findon) {
            System.out.println("未找到符合条件的数组");
        }
    }

    public double[] maopao(double[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            for (int k = 0; k < a.length - 1 - i; k++) {
                if (a[k] > a[k + 1]) {
                    double b = a[k];
                    a[k] = a[k + 1];
                    a[k + 1] = b;
                }
            }
        }
        return a;
    }

    public void startMath(List<Double> list, double sum) {
        if (someoneEqualSum) {// 先输出等于“和”本身的数  
            System.out.println("相加等于" + sum + "的数组为：");
            System.out.println(sum);
            System.out.println("-----------------------");
        }
        for (int i = 0; i <= list.size() - 2; i++) {
            findNumIndex.clear();
            findNumIndex.add(list.size() - 1 - i);// 记录第一个元素坐标  
            double indexNum = list.get(list.size() - 1 - i);// 从最大的元素开始，依次往前推  
            action(list, indexNum, list.size() - 1 - i, sum);
        }
    }

    /**
     * 递归方法
     *
     * @param list     被查询的数组
     * @param indexsum 当前元素相加的和
     * @param index    下一个元素位置
     * @param sum      要匹配的和
     */
    public void action(List<Double> list, double indexsum, int index, double sum) {
        if (index == 0)
            return;
        if (indexsum + list.get(index - 1) > sum) {// 元素【index-1】太大了，跳到下一个元素继续遍历  
            action(list, indexsum, index - 1, sum);
        } else if (indexsum + list.get(index - 1) < sum) {// 元素【index-1】可能符合条件，继续往下找  
            findNumIndex.add(index - 1);// 记录此元素坐标  
            indexsum = indexsum + list.get(index - 1);// 更新元素的和  
            action(list, indexsum, index - 1, sum);
        } else if (indexsum + list.get(index - 1) == sum) {
            findNumIndex.add(index - 1);
            findon = true;// 告诉系统找到了  
            System.out.println("相加等于" + sum + "的数组为：");
            for (int a : findNumIndex) {
                System.out.println(list.get(a));
            }
            System.out.println("-----------------------");
            return;
        }
    }

    public static void main(String[] args) {
        double[] a = {183478.5, 175587, 168995, 168085, 165654, 143421, 135372.5, 130829, 126935, 126900.5, 124212.5, 113729, 99062, 84436, 82333, 79723.5, 78193, 70526, 68022, 67118.5, 62685.5, 57327, 52759.5, 46810.5, 44944, 31316, 26764, 26672.5, 17809, 17635.5, 12173.5, 10169, 9417, 6205.5, 225};
        double sum = 1091318.5;
        CreateTest s = new CreateTest(a, sum);
        s.start();

        System.out.println(168995 + 168085 + 165654 + 143421 + 135372.5+113729+82333+113729);
    }
}  