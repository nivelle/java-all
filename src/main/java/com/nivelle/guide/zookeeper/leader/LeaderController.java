package com.nivelle.guide.zookeeper.leader;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
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
}
