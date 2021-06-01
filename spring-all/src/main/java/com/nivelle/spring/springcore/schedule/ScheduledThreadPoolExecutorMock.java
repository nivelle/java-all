package com.nivelle.spring.springcore.schedule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * ScheduledThreadPoolExecutor(定时任务线程池)
 *
 * @author nivelle
 * @date 2020/04/14
 */
public class ScheduledThreadPoolExecutorMock {


    /**
     * @Schedule
     *
     * 1、fixedDelay:控制方法执行的间隔时间,是以上一次方法执行完成后开始算起,如果上一次方法执行阻塞住了,那么直到上一次执行完,并间隔给定的时间后,执行下一次。

     * 2、fixedRate:是按照一定的速率执行,是从上一次方法执行开始的时间算起,如果上一次方法阻塞住了,下一次也是不会执行,但是在阻塞这段时间内累计应该执行的次数,当不再阻塞时,一下子把这些全部执行掉,而后再按照固定速率继续执行。
     *
     * 3、cron:表达式可以定制化执行任务,但是执行的方式是与fixedDelay相近的,也是会按照上一次方法结束时间开始算起。
     *
     * 4、initialDelay: 如,@Scheduled(initialDelay = 10000,fixedRate = 15000
     * 这个定时器就是在上一个的基础上加了一个initialDelay = 10000 意思就是在容器启动后,延迟10秒后再执行一次定时器,以后每15秒再执行一次该定时器。
     *
     */

    /**
     * （1）指定某个时刻执行任务,是通过延时队列的特性来解决的;
     * <p>
     * （2）重复执行,是通过在任务执行后再次把任务加入到队列中来解决的。
     */
    public static void main(String[] args) throws Exception {
        /**
         * // 默认定时任务线程池，最大线程数目不限制,keepLiveTime = 0 ,使用 延迟队列
         *  public ScheduledThreadPoolExecutor(int corePoolSize) {
         *         super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,new DelayedWorkQueue());
         *     }
         */
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);
        System.out.println("start:" + System.currentTimeMillis());

        /**
         * 将来执行的任务无返回值,有延迟:执行一个无返回值任务，5秒后执行，只执行一次
         *
         **/
        scheduledThreadPoolExecutor.schedule(() -> {
            System.out.println("spring:" + System.currentTimeMillis());
        }, 5, TimeUnit.SECONDS);
        /**
         * 将来执行的任务有返回值,有延迟:执行一个有返回值任务，5秒后执行，只执行一次
         */
        ScheduledFuture<String> future = scheduledThreadPoolExecutor.schedule(() -> {
            System.out.print("inner summer" + System.currentTimeMillis());
            return "outer summer:";
        }, 5, TimeUnit.SECONDS);

        System.out.println(future.get() + System.currentTimeMillis());
        /**
         *
         * 按固定频率执行一个任务，每2秒执行一次，1秒后执行;任务开始时的2秒后
         * **/
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            System.out.print("autumn:" + System.currentTimeMillis());
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        }, 1, 2, TimeUnit.SECONDS);

        /**
         * 按固定延时执行一个任务，每延时2秒执行一次，1秒后执行;
         */
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
            System.out.println("winter:" + System.currentTimeMillis());
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        }, 1, 2, TimeUnit.SECONDS);

    }
}
