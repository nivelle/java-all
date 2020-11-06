package com.nivelle.base.jdk.io;

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

/**
 *
 * DirectByteBuffer是Java用于实现堆外内存的一个重要类，通常用在通信过程中做缓冲池，如在Netty、MINA等NIO框架中应用广泛。
 * DirectByteBuffer对于堆外内存的创建、使用、销毁等逻辑均由Unsafe提供的堆外内存API来实现。
 * @author fuxinzhong
 * @date 2020/11/06
 */
public class DirectByteBufferDemo implements DirectBuffer {

    public static void main(String[] args) {
    }

    @Override
    public long address() {
        return 0;
    }

    @Override
    public Object attachment() {
        return null;
    }

    @Override
    public Cleaner cleaner() {
        return null;
    }
}
