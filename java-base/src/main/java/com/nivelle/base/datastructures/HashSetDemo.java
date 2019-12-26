package com.nivelle.base.datastructures;

import java.util.HashSet;

/**
 * HashSet
 *
 * @author fuxinzhong
 * @date 2019/10/15
 */
public class HashSetDemo {

    public static void main(String[] args) {
        HashSet hashSet = new HashSet();

        boolean aResult = hashSet.add("a");
        System.out.println("aResult is: " + aResult);

        boolean bResult = hashSet.add("b");
        System.out.println("bResult is: " + bResult);

        boolean cResult = hashSet.add("a");
        System.out.println("cResult is: " + cResult);

        java.util.Iterator iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            System.err.print(iterator.next());
        }

        hashSet.clear();
        System.out.println("clear result is:" + hashSet);


    }
}
