package com.nivelle.guide.distributed.rpc;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
@Slf4j
public class ServiceRegistry implements InitializingBean {

    private CountDownLatch countDownLatch = new CountDownLatch(1);


    @Autowired
    @Lazy
    private CuratorFramework curatorFramework;


    public void register(String data) {
        if (data != null) {
            createNode(data);
        }
    }

    private void createNode(String data) {
        try {
            byte[] bytes = data.getBytes();
            log.debug("create zookeeper node data={}", data);
            curatorFramework
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(Constant.ZK_DATA_PATH, bytes);
        } catch (KeeperException | InterruptedException e) {
            log.error("", e);
        } catch (Exception e) {
            log.error("create node error data ={}", data, e);
        }
    }


    /**
     * 创建 watcher 事件
     */
    private void addRootWatcher() throws Exception {

        Watcher watcher = (watchedEvent) -> {
            if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                countDownLatch.countDown();
            }
        };
        watcher.wait();
    }

    //创建父节点
    @Override
    public void afterPropertiesSet() {
        curatorFramework = curatorFramework.usingNamespace("register");
        String path = "/" + Constant.ZK_DATA_PATH;
        try {
            if (curatorFramework.checkExists().forPath(path) == null) {
                curatorFramework.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(path);
            }
            addRootWatcher();
            log.info("root path 的 watcher 事件创建成功");
        } catch (Exception e) {
            log.error("connect zookeeper fail，please check the log >> {}", e.getMessage(), e);
        }
    }


}
