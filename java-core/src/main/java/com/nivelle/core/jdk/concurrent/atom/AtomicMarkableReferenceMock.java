package com.nivelle.core.jdk.concurrent.atom;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/01/31
 */
public class AtomicMarkableReferenceMock {

    public static void main(String[] args) {
        /**
         *  主要用来避免ABA问题,给数据引用设置版本戳
         */
        String string1 = "aaa";
        String string2 = "bbb";

        AtomicMarkableReference atomicMarkableReference = new AtomicMarkableReference(string1, true);
    }
}
