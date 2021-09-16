package com.nivelle.core.javacore.concurrent.atom;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * AtomicStampedReference
 *
 * @author nivelle
 * @date 2020/04/13
 */
public class AtomicStampedReferenceMock {

    public static void main(String[] args) {
        /**
         *  主要用来避免ABA问题,给数据引用设置版本戳
         */
        String string1 = "aaa";
        String string2 = "bbb";

        AtomicStampedReference<String> atomicStampedReference = new AtomicStampedReference(string1, 1);
        atomicStampedReference.compareAndSet(string1, string2, atomicStampedReference.getStamp(), atomicStampedReference.getStamp() + 1);
        System.out.println("reference is:" + atomicStampedReference.getReference());

        boolean b = atomicStampedReference.attemptStamp(string2, atomicStampedReference.getStamp() + 1);
        System.out.println("b:" + b);
        System.out.println("reference is:" + atomicStampedReference.getStamp());


        boolean c = atomicStampedReference.weakCompareAndSet(string2, "ccc", 4, atomicStampedReference.getStamp() + 1);
        System.out.println("reference is:" + atomicStampedReference.getReference());
        System.out.println("c=" + c);

        boolean c2 = atomicStampedReference.compareAndSet(string2, "ccc", 4, atomicStampedReference.getStamp() + 1);
        System.out.println("reference is:" + atomicStampedReference.getReference());
        System.out.println("c2=" + c2);

    }
}
