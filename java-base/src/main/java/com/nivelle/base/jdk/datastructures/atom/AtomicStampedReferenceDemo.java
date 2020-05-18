package com.nivelle.base.jdk.datastructures.atom;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * AtomicStampedReference
 *
 * @author nivell
 * @date 2020/04/13
 */
public class AtomicStampedReferenceDemo {

    public static void main(String[] args) {
        AtomicStampedReference atomicStampedReference = new AtomicStampedReference(1,1);
    }
}
