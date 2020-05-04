package com.nivelle.base.javacore.datastructures.concurrent;

import java.util.Comparator;

/**
 * 优先队列比较器
 *
 * @author fuxinzhong
 * @date 2020/05/04
 */
public class PriorityBlockingQueueComparator implements Comparator<Float> {

    @Override
    public int compare(Float o1, Float o2) {
        if (((o1 > o2) ? false : true)) {
            return 1;
        } else if (o1 - o2 == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Float) {
            return false;
        }
        if (this.equals(obj)) {
            return true;
        }
        return false;
    }


}
