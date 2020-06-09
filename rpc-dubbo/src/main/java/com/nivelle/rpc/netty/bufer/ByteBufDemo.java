
package com.nivelle.rpc.netty.bufer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBufDemo {


    /**
     *
     * 1. 它可以被用户自定义的缓冲区类型扩展；
     * 2. 通过内置的复合缓冲区类型实现了透明的零拷贝；
     * 3. 容量可以按需增长（类似于JDK的StringBuilder）；
     * 4. 在读和写这两种模式之间切换不需要调用ByteBuffer的flip()方法；
     * 5. 读和写使用了不同的索引；
     * 6. 支持方法的链式调用；
     * 7. 支持引用计数；
     * 8. 支持池化。
     *
     */
    public static void main(String[] args) {
        // 创建一个缓冲区
        ByteBuf buffer = Unpooled.buffer(10);
        System.out.println("------------初始时缓冲区------------");
        printBuffer(buffer);
        // 添加一些数据到缓冲区中
        System.out.println("------------添加数据到缓冲区------------");
        String s = "love";
        buffer.writeBytes(s.getBytes());//将参数写入到buffer里面
        printBuffer(buffer);
        // 读取数据
        System.out.println("------------读取数据------------");
        while (buffer.isReadable()) {
            System.out.println((char) buffer.readByte());//读取buffer的数据
            printBuffer(buffer);
        }
        printBuffer(buffer);

        // 执行compact
        System.out.println("------------执行discardReadBytes------------");
        buffer.discardReadBytes();
        printBuffer(buffer);

        // 执行clear
        System.out.println("------------执行clear清空缓冲区------------");
        buffer.clear();
        printBuffer(buffer);

    }

    /**
     * 打印出ByteBuf的信息
     *
     * @param buffer
     */
    private static void printBuffer(ByteBuf buffer) {
        System.out.println("readerIndex：" + buffer.readerIndex());
        System.out.println("writerIndex：" + buffer.writerIndex());
        System.out.println("capacity：" + buffer.capacity());
    }
}
