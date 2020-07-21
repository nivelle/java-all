package com.nivelle.base.jdk.io;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2020/04/02
 */
public class NIOTest {

    /**
     * 传统IO的性能问题：
     * <p>
     * 1. 多次内存复制(数据先从外部设备复制到内核空间，再从内核空间复制到用户空间，这就发生了两次内存复制操作。这种操作会导致不必要的数据拷贝和上下文切换，从而降低 I/O 的性能。)
     * <p>
     * - JVM 会发出 read() 系统调用，并通过 read 系统调用向内核发起读请求；
     * <p>
     * - 内核向硬件发送读指令，并等待读就绪；
     * <p>
     * - 内核把将要读取的数据复制到指向的内核缓存中；
     * <p>
     * - 操作系统内核将数据复制到用户空间缓冲区，然后 read 系统调用返回
     * <p>
     * 2. 阻塞
     * <p>
     * - InputStream 的 read() 是一个 while 循环操作，它会一直等待数据读取，直到数据就绪才会返回。这就意味着如果没有数据就绪，这个读取操作将会一直被挂起，用户线程将会处于阻塞状态。
     * <p>
     * <p>
     * 优化操作：
     * <p>
     * - 使用缓冲区优化读写流操作: 传统 I/O 和 NIO 的最大区别就是传统 I/O 是面向流，NIO 是面向 Buffer;Buffer 可以将文件一次性读入内存再做后续处理，而传统的方式是边读文件边处理数据。虽然传统 I/O 后面也使用了缓冲块，例如 BufferedInputStream，但仍然不能和 NIO 相媲美。
     * <p>
     * - 使用 DirectBuffer 减少内存复制:普通的 Buffer 分配的是 JVM 堆内存，而 DirectBuffer 是直接分配物理内存 (非堆内存)
     * <p>
     * (1). DirectBuffer 申请的是非 JVM 的物理内存，所以创建和销毁的代价很高。DirectBuffer 申请的内存并不是直接由 JVM 负责垃圾回收，但在 DirectBuffer 包装类被回收时，会通过 Java Reference 机制来释放该内存块
     * <p>
     * (2). MappedByteBuffer，跟 DirectBuffer 不同的是，MappedByteBuffer 是通过本地类调用 mmap 进行文件内存映射的，map() 系统调用方法会直接将文件从硬盘拷贝到用户空间，只进行一次数据拷贝，从而减少了传统的 read() 方法从硬盘拷贝到内核空间这一步。
     * <p>
     * - 避免阻塞，优化 I/O 操作
     * <p>
     * (1) 由于线程池线程数量有限，一旦发生大量并发请求，超过最大数量的线程就只能等待，直到线程池中有空闲的线程可以被复用
     * <p>
     * (2) 对 Socket 的输入流进行读取时，读取流会一直阻塞. 除非有数据可读，连接释放，空指针或 I/O 异常。
     * <p>
     * 解决方法：
     * <p>
     * - 通道（Channel）: 1. Channel 有自己的处理器，可以完成内核空间和磁盘之间的 I/O 操作;  2. NIO 中，我们读取和写入数据都要通过 Channel，由于 Channel 是双向的，所以读、写可以同时进行
     * <p>
     * <p>
     * - 多路复用器（Selector）: 基于事件驱动实现，在selector上注册accept,read监听事件，selector会不断轮询注册在其上的 channel,如果某个channel上面发生监听事件，这个channel就处于就绪状态，然后进行io操作。
     * <p>
     * epoll:没有最大句柄1024的限制
     */
    public static void main(String[] args) {


    }
}
