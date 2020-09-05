package com.nivelle.spring.springcore.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 *
 * @author nivellefu
 */
@Component
public class SchedulerTask {


    private int count = 0;

    @Scheduled(cron = "0 0/1 * * * ?")
    private void process1() throws InterruptedException {
        System.out.println("this is process1 task runing before " + (count++));
        Thread.sleep(60000);
        System.out.println("this is process1 task runing  after " + (count++));
    }

    @Scheduled(initialDelay = 1000, fixedRate = 600)
    private void process2() {
        System.out.println("this is scheduler task runing  " + (count++));
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    private void process3() {
        System.out.println("this is process3 task runing before " + (count++));
        System.out.println("this is process3 task runing after " + (count++));

    }
}
