package com.nivelle.base.jdk.atom;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * AtomicStampedReference
 *
 * @author nivelle
 * @date 2020/04/13
 */
public class AtomicStampedReferenceDemo {

    public static void main(String[] args) {
        AtomicStampedReference atomicStampedReference = new AtomicStampedReference(1,1);
    }
}
