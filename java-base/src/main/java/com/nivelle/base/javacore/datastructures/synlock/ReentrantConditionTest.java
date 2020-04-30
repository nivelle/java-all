package com.nivelle.base.javacore.datastructures.synlock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Condition
 *
 * @author fuxinzhong
 * @date 2020/04/30
 */
public class ReentrantConditionTest {

    public static void main(String[] args) {
        ReentrantLock reentrantLock = new ReentrantLock();
        Condition condition = reentrantLock.newCondition();
    }
}
