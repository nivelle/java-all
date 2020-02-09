package com.nivelle.spring.springcore.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 *
 * @author nivellefu
 */
//@Component
public class SchedulerTask {


    private int count = 0;

    @Scheduled(cron = "*/6 * * * * ?")
    private void process1() throws InterruptedException {
        Thread.sleep(6000);
        System.out.println("this is process1 task runing  " + (count++));
    }

    @Scheduled(initialDelay = 1000, fixedRate = 6000)
    private void process2() {
        System.out.println("this is scheduler task runing  " + (count++));
    }

    @Scheduled(cron = "*/6 * * * * ?")
    private void process3() {
        System.out.println("this is process3 task runing  " + (count++));
    }
}
