package com.nivelle.container.tomcat;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * tomcat配置类
 *
 * @author nivelle
 * @date 2019/07/25
 */
@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Bean
    public TomcatGracefulShutdownListener gracefulShutdown() {
        return new TomcatGracefulShutdownListener();
    }


    @Bean
    public ConfigurableServletWebServerFactory webServerFactory(final TomcatGracefulShutdownListener gracefulShutdown) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(gracefulShutdown);
        factory.addAdditionalTomcatConnectors(createConnector());
        return factory;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory server) {
        server.setPort(8080);
        ((TomcatServletWebServerFactory) server).addConnectorCustomizers((Connector connector) -> {
            connector.setParseBodyMethods("get");
            Http11NioProtocol protocolHandler = (Http11NioProtocol) connector.getProtocolHandler();
            protocolHandler.setMaxConnections(3);
            protocolHandler.setMaxThreads(1);
            protocolHandler.setSelectorTimeout(1000);
            protocolHandler.setSessionTimeout(1000);
            protocolHandler.setConnectionTimeout(1000);
        });
    }

    private Connector createConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(8090);
        connector.setAllowTrace(true);
        Http11NioProtocol protocolHandler = (Http11NioProtocol) connector.getProtocolHandler();
        // 最大线程数
        protocolHandler.setMaxThreads(1);
        // 最大连接数
        protocolHandler.setMaxConnections(3);
        //阻塞队列长度
        protocolHandler.setAcceptCount(1);
        return connector;
    }

    /**
     *
     * 1. 当一个线程占有一个锁的时候，线程堆栈会打印一个－locked<0x22bffb60>
     * 2. 当一个线程正在等在其他线程释放该锁，线程堆栈会打印一个－waiting to lock<0x22bffb60>
     * 3. 当一个线程占有一个锁，但又执行在该锁的wait上，线程堆栈中首先打印locked,然后打印－waiting on <0x22c03c60>
     *
     *
     * 超过最大连接数的时候则阻塞:
     *
     * 2020-07-21 23:30:32
     * Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.191-b12 mixed mode):
     *
     * "Attach Listener" #52 daemon prio=9 os_prio=31 tid=0x00007ff52290c000 nid=0x9507 waiting on condition [0x0000000000000000]
     *    java.lang.Thread.State: RUNNABLE
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "logback-8" #51 daemon prio=5 os_prio=31 tid=0x00007ff522920800 nid=0x600b waiting on condition [0x000070000effc000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000074017a508> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "logback-7" #50 daemon prio=5 os_prio=31 tid=0x00007ff52291d000 nid=0x5507 waiting on condition [0x000070000edf6000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000074017a508> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "logback-6" #49 daemon prio=5 os_prio=31 tid=0x00007ff52321c000 nid=0x6207 waiting on condition [0x000070000ecf3000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000074017a508> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "logback-5" #48 daemon prio=5 os_prio=31 tid=0x00007ff52291c800 nid=0xa607 waiting on condition [0x000070000e0cf000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000074017a508> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "logback-4" #47 daemon prio=5 os_prio=31 tid=0x00007ff523211000 nid=0x9707 waiting on condition [0x000070000dec9000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000074017a508> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "logback-3" #46 daemon prio=5 os_prio=31 tid=0x00007ff52291a000 nid=0x640b waiting on condition [0x000070000f0ff000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000074017a508> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "logback-2" #45 daemon prio=5 os_prio=31 tid=0x00007ff522ab3800 nid=0x4d07 waiting on condition [0x000070000eef9000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000074017a508> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "DestroyJavaVM" #37 prio=5 os_prio=31 tid=0x00007ff522658000 nid=0xe03 waiting on condition [0x0000000000000000]
     *    java.lang.Thread.State: RUNNABLE
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "http-nio-8090-Acceptor" #35 daemon prio=5 os_prio=31 tid=0x00007ff523c02000 nid=0x5d03 runnable [0x000070000ebf0000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at sun.nio.ch.ServerSocketChannelImpl.accept0(Native Method)
     * 	at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:422)
     * 	at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:250)
     * 	- locked <0x000000079c65c118> (a java.lang.Object)
     * 	at org.apache.tomcat.util.net.NioEndpoint.serverSocketAccept(NioEndpoint.java:469)
     * 	at org.apache.tomcat.util.net.NioEndpoint.serverSocketAccept(NioEndpoint.java:71)
     * 	at org.apache.tomcat.util.net.Acceptor.run(Acceptor.java:95)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "http-nio-8090-ClientPoller" #34 daemon prio=5 os_prio=31 tid=0x00007ff523c01800 nid=0x9803 runnable [0x000070000eaed000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at sun.nio.ch.KQueueArrayWrapper.kevent0(Native Method)
     * 	at sun.nio.ch.KQueueArrayWrapper.poll(KQueueArrayWrapper.java:198)
     * 	at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:117)
     * 	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
     * 	- locked <0x0000000797ef03b8> (a sun.nio.ch.Util$3)
     * 	- locked <0x0000000797ef03a8> (a java.util.Collections$UnmodifiableSet)
     * 	- locked <0x0000000797ef0288> (a sun.nio.ch.KQueueSelectorImpl)
     * 	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
     * 	at org.apache.tomcat.util.net.NioEndpoint$Poller.run(NioEndpoint.java:709)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "http-nio-8090-exec-1" #33 daemon prio=5 os_prio=31 tid=0x00007ff523bfc800 nid=0x9a03 waiting on condition [0x000070000e9ea000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x0000000797eef940> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
     * 	at org.apache.tomcat.util.threads.TaskQueue.take(TaskQueue.java:107)
     * 	at org.apache.tomcat.util.threads.TaskQueue.take(TaskQueue.java:33)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "http-nio-8080-Acceptor" #32 daemon prio=5 os_prio=31 tid=0x00007ff522a9e000 nid=0x9c03 runnable [0x000070000e8e7000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at sun.nio.ch.ServerSocketChannelImpl.accept0(Native Method)
     * 	at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:422)
     * 	at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:250)
     * 	- locked <0x0000000797e364b8> (a java.lang.Object)
     * 	at org.apache.tomcat.util.net.NioEndpoint.serverSocketAccept(NioEndpoint.java:469)
     * 	at org.apache.tomcat.util.net.NioEndpoint.serverSocketAccept(NioEndpoint.java:71)
     * 	at org.apache.tomcat.util.net.Acceptor.run(Acceptor.java:95)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "http-nio-8080-ClientPoller" #31 daemon prio=5 os_prio=31 tid=0x00007ff523216800 nid=0x9d03 runnable [0x000070000e7e4000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at sun.nio.ch.KQueueArrayWrapper.kevent0(Native Method)
     * 	at sun.nio.ch.KQueueArrayWrapper.poll(KQueueArrayWrapper.java:198)
     * 	at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:117)
     * 	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
     * 	- locked <0x0000000797ecfb70> (a sun.nio.ch.Util$3)
     * 	- locked <0x0000000797ecfb60> (a java.util.Collections$UnmodifiableSet)
     * 	- locked <0x0000000797ecfa40> (a sun.nio.ch.KQueueSelectorImpl)
     * 	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
     * 	at org.apache.tomcat.util.net.NioEndpoint$Poller.run(NioEndpoint.java:709)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "http-nio-8080-exec-1" #30 daemon prio=5 os_prio=31 tid=0x00007ff523bfc000 nid=0x9f03 waiting on condition [0x000070000e6e1000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x0000000797ea0ea8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
     * 	at org.apache.tomcat.util.threads.TaskQueue.take(TaskQueue.java:107)
     * 	at org.apache.tomcat.util.threads.TaskQueue.take(TaskQueue.java:33)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "http-nio-8080-BlockPoller" #29 daemon prio=5 os_prio=31 tid=0x00007ff523208800 nid=0xa007 runnable [0x000070000e5de000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at sun.nio.ch.KQueueArrayWrapper.kevent0(Native Method)
     * 	at sun.nio.ch.KQueueArrayWrapper.poll(KQueueArrayWrapper.java:198)
     * 	at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:117)
     * 	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
     * 	- locked <0x0000000797e36810> (a sun.nio.ch.Util$3)
     * 	- locked <0x0000000797e36800> (a java.util.Collections$UnmodifiableSet)
     * 	- locked <0x0000000797e366e0> (a sun.nio.ch.KQueueSelectorImpl)
     * 	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
     * 	at org.apache.tomcat.util.net.NioBlockingSelector$BlockPoller.run(NioBlockingSelector.java:313)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "container-0" #27 prio=5 os_prio=31 tid=0x00007ff5231ff000 nid=0xa103 waiting on condition [0x000070000e4db000]
     *    java.lang.Thread.State: TIMED_WAITING (sleeping)
     * 	at java.lang.Thread.sleep(Native Method)
     * 	at org.apache.catalina.core.StandardServer.await(StandardServer.java:570)
     * 	at org.springframework.boot.web.embedded.tomcat.TomcatWebServer$1.run(TomcatWebServer.java:179)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "Catalina-utility-2" #26 prio=1 os_prio=31 tid=0x00007ff52263d000 nid=0xa303 waiting on condition [0x000070000e3d8000]
     *    java.lang.Thread.State: WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000079c65e4d0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "Catalina-utility-1" #25 prio=1 os_prio=31 tid=0x00007ff522a8c800 nid=0x5707 waiting on condition [0x000070000e2d5000]
     *    java.lang.Thread.State: TIMED_WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000079c65e4d0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "http-nio-8090-BlockPoller" #24 daemon prio=5 os_prio=31 tid=0x00007ff523baa800 nid=0x3d13 runnable [0x000070000ddc6000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at sun.nio.ch.KQueueArrayWrapper.kevent0(Native Method)
     * 	at sun.nio.ch.KQueueArrayWrapper.poll(KQueueArrayWrapper.java:198)
     * 	at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:117)
     * 	at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
     * 	- locked <0x000000079c65bae0> (a sun.nio.ch.Util$3)
     * 	- locked <0x000000079c65bad0> (a java.util.Collections$UnmodifiableSet)
     * 	- locked <0x000000079c65b990> (a sun.nio.ch.KQueueSelectorImpl)
     * 	at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
     * 	at org.apache.tomcat.util.net.NioBlockingSelector$BlockPoller.run(NioBlockingSelector.java:313)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "logback-1" #19 daemon prio=5 os_prio=31 tid=0x00007ff523854800 nid=0xa507 waiting on condition [0x000070000e1d2000]
     *    java.lang.Thread.State: TIMED_WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x000000074017a508> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "RMI Scheduler(0)" #16 daemon prio=5 os_prio=31 tid=0x00007ff522a15800 nid=0xa803 waiting on condition [0x000070000dfcc000]
     *    java.lang.Thread.State: TIMED_WAITING (parking)
     * 	at sun.misc.Unsafe.park(Native Method)
     * 	- parking to wait for  <0x00000007401c79f8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     * 	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
     * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)
     * 	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
     * 	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
     * 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
     * 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "RMI TCP Accept-0" #13 daemon prio=5 os_prio=31 tid=0x00007ff5239f4800 nid=0x3a03 runnable [0x000070000dbc0000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at java.net.PlainSocketImpl.socketAccept(Native Method)
     * 	at java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:409)
     * 	at java.net.ServerSocket.implAccept(ServerSocket.java:545)
     * 	at java.net.ServerSocket.accept(ServerSocket.java:513)
     * 	at sun.management.jmxremote.LocalRMIServerSocketFactory$1.accept(LocalRMIServerSocketFactory.java:52)
     * 	at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.executeAcceptLoop(TCPTransport.java:405)
     * 	at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.run(TCPTransport.java:377)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "RMI TCP Accept-61714" #12 daemon prio=5 os_prio=31 tid=0x00007ff5230c3000 nid=0x4103 runnable [0x000070000dabd000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at java.net.PlainSocketImpl.socketAccept(Native Method)
     * 	at java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:409)
     * 	at java.net.ServerSocket.implAccept(ServerSocket.java:545)
     * 	at java.net.ServerSocket.accept(ServerSocket.java:513)
     * 	at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.executeAcceptLoop(TCPTransport.java:405)
     * 	at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.run(TCPTransport.java:377)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "RMI TCP Accept-0" #11 daemon prio=5 os_prio=31 tid=0x00007ff523980800 nid=0x4303 runnable [0x000070000d9ba000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at java.net.PlainSocketImpl.socketAccept(Native Method)
     * 	at java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:409)
     * 	at java.net.ServerSocket.implAccept(ServerSocket.java:545)
     * 	at java.net.ServerSocket.accept(ServerSocket.java:513)
     * 	at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.executeAcceptLoop(TCPTransport.java:405)
     * 	at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.run(TCPTransport.java:377)
     * 	at java.lang.Thread.run(Thread.java:748)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "Service Thread" #9 daemon prio=9 os_prio=31 tid=0x00007ff523967800 nid=0x4403 runnable [0x0000000000000000]
     *    java.lang.Thread.State: RUNNABLE
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "C1 CompilerThread2" #8 daemon prio=9 os_prio=31 tid=0x00007ff523966800 nid=0x4503 waiting on condition [0x0000000000000000]
     *    java.lang.Thread.State: RUNNABLE
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "C2 CompilerThread1" #7 daemon prio=9 os_prio=31 tid=0x00007ff522190000 nid=0x4603 waiting on condition [0x0000000000000000]
     *    java.lang.Thread.State: RUNNABLE
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "C2 CompilerThread0" #6 daemon prio=9 os_prio=31 tid=0x00007ff522867800 nid=0x3603 waiting on condition [0x0000000000000000]
     *    java.lang.Thread.State: RUNNABLE
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "Monitor Ctrl-Break" #5 daemon prio=5 os_prio=31 tid=0x00007ff523913000 nid=0x4903 runnable [0x000070000d4ab000]
     *    java.lang.Thread.State: RUNNABLE
     * 	at java.net.SocketInputStream.socketRead0(Native Method)
     * 	at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
     * 	at java.net.SocketInputStream.read(SocketInputStream.java:171)
     * 	at java.net.SocketInputStream.read(SocketInputStream.java:141)
     * 	at sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:284)
     * 	at sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:326)
     * 	at sun.nio.cs.StreamDecoder.read(StreamDecoder.java:178)
     * 	- locked <0x000000074031ae68> (a java.io.InputStreamReader)
     * 	at java.io.InputStreamReader.read(InputStreamReader.java:184)
     * 	at java.io.BufferedReader.fill(BufferedReader.java:161)
     * 	at java.io.BufferedReader.readLine(BufferedReader.java:324)
     * 	- locked <0x000000074031ae68> (a java.io.InputStreamReader)
     * 	at java.io.BufferedReader.readLine(BufferedReader.java:389)
     * 	at com.intellij.rt.execution.application.AppMainV2$1.run(AppMainV2.java:64)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "Signal Dispatcher" #4 daemon prio=9 os_prio=31 tid=0x00007ff522047000 nid=0x3403 runnable [0x0000000000000000]
     *    java.lang.Thread.State: RUNNABLE
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "Finalizer" #3 daemon prio=8 os_prio=31 tid=0x00007ff522808800 nid=0x2e03 in Object.wait() [0x000070000d19f000]
     *    java.lang.Thread.State: WAITING (on object monitor)
     * 	at java.lang.Object.wait(Native Method)
     * 	- waiting on <0x0000000740284bf8> (a java.lang.ref.ReferenceQueue$Lock)
     * 	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)
     * 	- locked <0x0000000740284bf8> (a java.lang.ref.ReferenceQueue$Lock)
     * 	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:165)
     * 	at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:216)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "Reference Handler" #2 daemon prio=10 os_prio=31 tid=0x00007ff523016800 nid=0x2c03 in Object.wait() [0x000070000d09c000]
     *    java.lang.Thread.State: WAITING (on object monitor)
     * 	at java.lang.Object.wait(Native Method)
     * 	- waiting on <0x00000007402f9130> (a java.lang.ref.Reference$Lock)
     * 	at java.lang.Object.wait(Object.java:502)
     * 	at java.lang.ref.Reference.tryHandlePending(Reference.java:191)
     * 	- locked <0x00000007402f9130> (a java.lang.ref.Reference$Lock)
     * 	at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)
     *
     *    Locked ownable synchronizers:
     * 	- None
     *
     * "VM Thread" os_prio=31 tid=0x00007ff523831800 nid=0x5303 runnable
     *
     * "GC task thread#0 (ParallelGC)" os_prio=31 tid=0x00007ff522013800 nid=0x2107 runnable
     *
     * "GC task thread#1 (ParallelGC)" os_prio=31 tid=0x00007ff522014800 nid=0x2003 runnable
     *
     * "GC task thread#2 (ParallelGC)" os_prio=31 tid=0x00007ff522015000 nid=0x1f03 runnable
     *
     * "GC task thread#3 (ParallelGC)" os_prio=31 tid=0x00007ff522015800 nid=0x2a03 runnable
     *
     * "VM Periodic Task Thread" os_prio=31 tid=0x00007ff523a01000 nid=0x3c03 waiting on condition
     *
     * JNI global references: 1125
     */
}
