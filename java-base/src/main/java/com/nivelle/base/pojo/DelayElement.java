package com.nivelle.base.pojo;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延时队列存储的元素
 *
 * @author fuxinzhong
 * @date 2020/05/04
 */
public class DelayElement implements Delayed {
    Long index;

    public DelayElement(Long index) {
        this.index = index;
    }

    public long getDelay(TimeUnit unit) {
        return this.index;
    }

    public boolean setIndex(long index) {
        this.index = index;
        return true;
    }

    @Override
    public String toString() {
        return "DelayElement{" +
                "index=" + index +
                '}';
    }

    public int compareTo(Delayed delayed) {
        if (this.index > delayed.getDelay(TimeUnit.SECONDS)) {
            return 1;
        } else if (this.index == delayed.getDelay(TimeUnit.SECONDS)) {
            return 0;
        } else {
            return -1;
        }
    }

}
