package com.nivelle.guide.zookeeper.leader;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
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
@RequestMapping("/zk")
@Slf4j
public class LeaderController {

    private static final String PATH = "/zk/leader";
    private static final String PATH2 = "/zk/leader2";


    @Autowired
    @Lazy
    private CuratorFramework curatorFramework;


    @RequestMapping("leaderSelector")
    public String leaderSelector() {
        SelectorClient example = new SelectorClient(curatorFramework, PATH, "jessy");
        example.start();
        SelectorClient example2 = new SelectorClient(curatorFramework, PATH, "fuck");
        example2.start();
        return "success";
    }

    @RequestMapping("leaderLatch")
    public String leaderLatch() {
        try {
            LeaderLatch leaderLatch1 = new LeaderLatch(curatorFramework, PATH, "jessy");
            leaderLatch1.start();
            LeaderLatch leaderLatch2 = new LeaderLatch(curatorFramework, PATH, "fuck");
            leaderLatch2.start();
            Boolean isLeader = leaderLatch1.hasLeadership();
            if (isLeader) {
                log.info(leaderLatch1.getId() + "is leader");
                leaderLatch1.close();
            }else {
                leaderLatch1.await();
            }
            Boolean isLeader2 = leaderLatch2.hasLeadership();
            if (isLeader2) {
                log.info(leaderLatch2.getId() + "is leader");
                leaderLatch2.close();
            }
        } catch (Exception e) {
            log.error("leaderLatch is error ", e);
        }
        return "success";
    }
}
