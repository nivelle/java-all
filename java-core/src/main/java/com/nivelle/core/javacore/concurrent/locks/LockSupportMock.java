package com.nivelle.core.javacore.concurrent.locks;

import java.util.concurrent.locks.LockSupport;

/**
 * LockSupport
 *
 * @author fuxinzhong
 * @date 2020/05/06
 */
public class LockSupportMock {

    public static void main(String[] args) {
        /**
         * public static void park(Object blocker) {
         *    //获取当前线程
         *     Thread t = Thread.currentThread();
         *    //记录当前线程阻塞的原因,底层就是unsafe.putObject,就是把对象存储起来
         *     setBlocker(t, blocker);
         *     //执行park
         *     unsafe.park(false, 0L);
         *    //线程恢复后，去掉阻塞原因
         *     setBlocker(t, null);
         * }
         */
        Thread currentThread = Thread.currentThread();
        System.out.println(currentThread.getId() + ",before state:" + currentThread.getState());
        LockSupport.park();
        System.out.println(currentThread.getId() + ",after state:" + currentThread.getState());
        LockSupport.unpark(currentThread);
        /**
         * nivelleMac:java-guides nivellefu$ jstack -l 75445
         * 2020-11-04 23:02:34
         * Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.191-b12 mixed mode):
         *
         * "Attach Listener" #10 daemon prio=9 os_prio=31 tid=0x00007fd34f0f0800 nid=0x4b07 waiting on condition [0x0000000000000000]
         *    java.lang.Thread.State: RUNNABLE
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "Service Thread" #9 daemon prio=9 os_prio=31 tid=0x00007fd3501a9000 nid=0x4203 runnable [0x0000000000000000]
         *    java.lang.Thread.State: RUNNABLE
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "C1 CompilerThread2" #8 daemon prio=9 os_prio=31 tid=0x00007fd34f0ef000 nid=0x3503 waiting on condition [0x0000000000000000]
         *    java.lang.Thread.State: RUNNABLE
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "C2 CompilerThread1" #7 daemon prio=9 os_prio=31 tid=0x00007fd34fa2a800 nid=0x3403 waiting on condition [0x0000000000000000]
         *    java.lang.Thread.State: RUNNABLE
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "C2 CompilerThread0" #6 daemon prio=9 os_prio=31 tid=0x00007fd34fa31000 nid=0x4503 waiting on condition [0x0000000000000000]
         *    java.lang.Thread.State: RUNNABLE
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "Monitor Ctrl-Break" #5 daemon prio=5 os_prio=31 tid=0x00007fd3508bb800 nid=0x4603 runnable [0x000070000d635000]
         *    java.lang.Thread.State: RUNNABLE
         *         at java.net.SocketInputStream.socketRead0(Native Method)
         *         at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
         *         at java.net.SocketInputStream.read(SocketInputStream.java:171)
         *         at java.net.SocketInputStream.read(SocketInputStream.java:141)
         *         at sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:284)
         *         at sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:326)
         *         at sun.nio.cs.StreamDecoder.read(StreamDecoder.java:178)
         *         - locked <0x0000000795856810> (a java.io.InputStreamReader)
         *         at java.io.InputStreamReader.read(InputStreamReader.java:184)
         *         at java.io.BufferedReader.fill(BufferedReader.java:161)
         *         at java.io.BufferedReader.readLine(BufferedReader.java:324)
         *         - locked <0x0000000795856810> (a java.io.InputStreamReader)
         *         at java.io.BufferedReader.readLine(BufferedReader.java:389)
         *         at com.intellij.rt.execution.application.AppMainV2$1.run(AppMainV2.java:64)
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "Signal Dispatcher" #4 daemon prio=9 os_prio=31 tid=0x00007fd35001e000 nid=0x3203 runnable [0x0000000000000000]
         *    java.lang.Thread.State: RUNNABLE
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "Finalizer" #3 daemon prio=8 os_prio=31 tid=0x00007fd34f03d800 nid=0x5103 in Object.wait() [0x000070000d329000]
         *    java.lang.Thread.State: WAITING (on object monitor)
         *         at java.lang.Object.wait(Native Method)
         *         - waiting on <0x0000000795588ed0> (a java.lang.ref.ReferenceQueue$Lock)
         *         at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)
         *         - locked <0x0000000795588ed0> (a java.lang.ref.ReferenceQueue$Lock)
         *         at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:165)
         *         at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:216)
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "Reference Handler" #2 daemon prio=10 os_prio=31 tid=0x00007fd35082a000 nid=0x5303 in Object.wait() [0x000070000d226000]
         *    java.lang.Thread.State: WAITING (on object monitor)
         *         at java.lang.Object.wait(Native Method)
         *         - waiting on <0x0000000795586bf8> (a java.lang.ref.Reference$Lock)
         *         at java.lang.Object.wait(Object.java:502)
         *         at java.lang.ref.Reference.tryHandlePending(Reference.java:191)
         *         - locked <0x0000000795586bf8> (a java.lang.ref.Reference$Lock)
         *         at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "main" #1 prio=5 os_prio=31 tid=0x00007fd34f80d000 nid=0xf03 waiting on condition [0x000070000cc14000]
         *    java.lang.Thread.State: WAITING (parking)
         *         at sun.misc.Unsafe.park(Native Method)
         *         at java.util.concurrent.locks.LockSupport.park(LockSupport.java:304)
         *         at com.nivelle.base.jdk.concurrent.locks.LockSupportDemo.main(LockSupportDemo.java:28)
         *
         *    Locked ownable synchronizers:
         *         - None
         *
         * "VM Thread" os_prio=31 tid=0x00007fd34f81e800 nid=0x2c03 runnable
         *
         * "GC task thread#0 (ParallelGC)" os_prio=31 tid=0x00007fd35080a800 nid=0x1b07 runnable
         *
         * "GC task thread#1 (ParallelGC)" os_prio=31 tid=0x00007fd350009800 nid=0x1c03 runnable
         *
         * "GC task thread#2 (ParallelGC)" os_prio=31 tid=0x00007fd34f80e800 nid=0x1f03 runnable
         *
         * "GC task thread#3 (ParallelGC)" os_prio=31 tid=0x00007fd35000a800 nid=0x2a03 runnable
         *
         * "VM Periodic Task Thread" os_prio=31 tid=0x00007fd3501e2000 nid=0x3703 waiting on condition
         *
         * JNI global references: 15
         */
    }
}
