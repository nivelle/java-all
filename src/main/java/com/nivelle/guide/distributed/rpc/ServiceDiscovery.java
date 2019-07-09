package com.nivelle.guide.distributed.rpc;

import io.netty.util.internal.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
@Slf4j
public class ServiceDiscovery implements InitializingBean {

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<>();

    @Autowired
    @Lazy
    private CuratorFramework curatorFramework;


    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                log.debug("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                log.debug("using random data: {}", data);
            }
        }
        return data;
    }

    /**
     * 创建 watcher 事件
     */
    private void watchChildNode() throws Exception {

        curatorFramework = curatorFramework.usingNamespace("register");
        PathChildrenCache cache = new PathChildrenCache(curatorFramework,
                Constant.ZK_DATA_PATH, false);

        cache.start();
        cache.getListenable().addListener((CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) -> {
            if (pathChildrenCacheEvent.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                List<String> nodeList = curatorFramework.getChildren().forPath(Constant.ZK_DATA_PATH);
                for (int i = 0; i < nodeList.size(); i++) {
                    dataList.add(new String(curatorFramework.getData().forPath(Constant.ZK_DATA_PATH + "/" + nodeList.get(i))));
                }
            } else if (pathChildrenCacheEvent.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                List<String> nodeList = curatorFramework.getChildren().forPath(Constant.ZK_DATA_PATH);
                for (int i = 0; i < nodeList.size(); i++) {
                    dataList.add(new String(curatorFramework.getData().forPath(Constant.ZK_DATA_PATH + "/" + nodeList.get(i))));
                }
            } else if (pathChildrenCacheEvent.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                List<String> nodeList = curatorFramework.getChildren().forPath(Constant.ZK_DATA_PATH);
                for (int i = 0; i < nodeList.size(); i++) {
                    dataList.add(new String(curatorFramework.getData().forPath(Constant.ZK_DATA_PATH + "/" + nodeList.get(i))));
                }
            }
        });


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
            watchChildNode();
        } catch (Exception e) {
            log.error("connect zookeeper fail，please check the log >> {}", e.getMessage(), e);
        }
        log.info("服务发现注册成功");
    }


}
