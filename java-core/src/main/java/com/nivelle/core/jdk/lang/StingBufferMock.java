package com.nivelle.core.jdk.lang;

/**
 * StringBuffer
 *
 * @author fuxinzhong
 * @date 2021/02/03
 */
public class StingBufferMock {

    public static void main(String[] args) {
        /**
         *  public final class StringBuffer extends AbstractStringBuilder implements java.io.Serializable, CharSequence
         */
        StringBuffer stringBuffer1 = new StringBuffer();
        System.out.println(stringBuffer1.capacity());


        StringBuffer stringBuffer2 = new StringBuffer(32);
        System.out.println(stringBuffer2.capacity());


        StringBuffer stringBuffer3 = new StringBuffer("nivelle");
        System.out.println(stringBuffer3.capacity());


    }
}
