package com.nivelle.spring.zookeeper.lock;

/**
 * curator
 *
 * @author fuxinzhong
 * @date 2019/07/06
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test/curator")
@Slf4j
public class CuratorController {

    @Autowired
    private DistributedLockByCurator distributedLockByZookeeper;

    private final static String PATH = "test";

    @GetMapping("/lock1")
    public Boolean getLock1() {
        Boolean flag;
        distributedLockByZookeeper.acquireDistributedLock(PATH);
        try {
            log.info("I am lock1，i am updating resource……！！！");
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            flag = distributedLockByZookeeper.releaseDistributedLock(PATH);
        }
        return flag;
    }

    @GetMapping("/lock2")
    public Boolean getLock2() {
        Boolean flag;
        distributedLockByZookeeper.acquireDistributedLock(PATH);
        try {
            log.info("I am lock2，i am updating resource……！！！");
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            flag = distributedLockByZookeeper.releaseDistributedLock(PATH);
        }
        return flag;
    }
}
