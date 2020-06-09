
package com.nivelle.rpc.netty.bufer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBufCompositeBufferDemo {


    /**
     * 复合缓冲区
     */
    public static void main(String[] args) {

        // 创建一个堆缓冲区
        ByteBuf heapBuf = Unpooled.buffer(3);
        String way = "nivelle";
        heapBuf.writeBytes(way.getBytes());
        // 创建一个直接缓冲区
        ByteBuf directBuf = Unpooled.directBuffer(3);
        String lau = "fuck";
        directBuf.writeBytes(lau.getBytes());

        // 创建一个复合缓冲区
        CompositeByteBuf compositeBuffer = Unpooled.compositeBuffer(10);
        /**
         * 将堆缓冲区和直接缓冲区添加到复合缓冲区
         */
        compositeBuffer.addComponents(heapBuf, directBuf);
        /**
         * 检查是否是支撑数组
         *
         * 不是支撑数组，则为复合缓冲区
         */
        if (!compositeBuffer.hasArray()) {
            /**
             * 复合缓冲区的数据分别处理
             */
            for (ByteBuf buffer : compositeBuffer) {
                // 计算第一个字节的偏移量
                int offset = buffer.readerIndex();
                // 可读字节数
                int length = buffer.readableBytes();
                // 获取字节内容
                byte[] array = new byte[length];
                buffer.getBytes(offset, array);
                printBuffer(array, offset, length);
            }
        }
    }
    private static void printBuffer(byte[] array, int offset, int len) {
        System.out.println("array：" + array);
        System.out.println("array->String：" + new String(array));
        System.out.println("offset：" + offset);
        System.out.println("len：" + len);
    }
}
