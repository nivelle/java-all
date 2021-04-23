package com.nivelle.rpc.netty.pool;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

/**
 * 内存池化性能对比
 *
 * @author fuxinzhong
 * @date 2021/04/23
 */
public class NettyPoolMock {




    @Test
    public void pooledBuf() {
        final byte[] CONTENT = new byte[1024];
        int loop = 1000000;
        long startTime = System.currentTimeMillis();

        ByteBuf poolBuffer = null;
        for (int i = 0; i < loop; i++) {
            poolBuffer = PooledByteBufAllocator.DEFAULT.directBuffer(1024);
            poolBuffer.writeBytes(CONTENT);
            poolBuffer.release();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("内存池分配缓冲区耗时；" + (endTime - startTime));
    }

    @Test
    public void directBuf(){
        final byte[] CONTENT = new byte[1024];
        int loop = 1000000;
        long startTime = System.currentTimeMillis();

        ByteBuf poolBuffer = null;
        for (int i = 0; i < loop; i++) {
            poolBuffer = Unpooled.directBuffer(1024);
            poolBuffer.writeBytes(CONTENT);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("直接内存分配耗时3212；" + (endTime - startTime));
    }
}
