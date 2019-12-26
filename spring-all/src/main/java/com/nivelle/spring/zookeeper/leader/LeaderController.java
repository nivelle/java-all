package com.nivelle.spring.zookeeper.leader;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2019/07/09
 */

@RestController
@RequestMapping("test/zk")
@Slf4j
public class LeaderController {

    private static final String PATH = "/zk/leader";
    private static final String PATH2 = "/zk/leader2";


    @Autowired
    @Lazy
    private CuratorFramework curatorFramework;

    /**
     * LeaderSelectorListener可以对领导权进行控制， 在适当的时候释放领导权，这样每个节点都有可能获得领导权
     * @return
     */
    @RequestMapping("leaderSelector")
    public String leaderSelector() {
        SelectorClient example = new SelectorClient(curatorFramework, PATH, "jessy");
        example.start();
        SelectorClient example2 = new SelectorClient(curatorFramework, PATH, "fuck");
        example2.start();
        return "success";
    }

    /**
     * LeaderLatch一直持有leadership， 除非调用close方法，否则它不会释放领导权。
     * @return
     */
    @RequestMapping("leaderLatch")
    public String leaderLatch() {
        try {
            LeaderLatch leaderLatch1 = new LeaderLatch(curatorFramework, PATH, "jessy");
            leaderLatch1.addListener(new LeaderLatchListener() {
                @Override
                public void isLeader() {
                    System.out.println(leaderLatch1.getId() + "leaderLatch1 is leader");
                }

                @Override
                public void notLeader() {
                    System.out.println(leaderLatch1.getId() + "leaderLatch1 is not leader");
                }
            });

            LeaderLatch leaderLatch2 = new LeaderLatch(curatorFramework, PATH, "fuck");
            leaderLatch2.addListener(new LeaderLatchListener() {
                @Override
                public void isLeader() {
                    System.out.println(leaderLatch2.getId() + "leaderLatch2 is leader");
                }

                @Override
                public void notLeader() {
                    System.out.println(leaderLatch2.getId() + "leaderLatch2 is not leader");
                }
            });
            leaderLatch2.start();
            leaderLatch1.start();
        } catch (Exception e) {
            log.error("leaderLatch is error ", e);
        }
        return "success";
    }
}
