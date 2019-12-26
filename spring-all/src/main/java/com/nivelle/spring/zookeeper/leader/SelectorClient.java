package com.nivelle.spring.zookeeper.leader;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.logging.log4j.util.Strings;

import java.io.Closeable;

/**
 * 选主
 *
 * @author fuxinzhong
 * @date 2019/07/09
 */

@Slf4j
public class SelectorClient extends LeaderSelectorListenerAdapter implements Closeable {

    private String name;
    private LeaderSelector leaderSelector;

    public SelectorClient(CuratorFramework client, String path, String name) {
        this.name = name;
        leaderSelector = new LeaderSelector(client, path, this);
        //leaderSelector.autoRequeue();//保证在此实例释放领导权之后还可能获得领导权。
    }

    public void start() {
        leaderSelector.start();
    }

    @Override
    public void close() {
        leaderSelector.close();
    }


    //当实例被选为leader之后，调用takeLeadership方法进行业务逻辑处理，处理完成即释放领导权。
    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
        int i = 1;
        while (i <= 5) {
            i++;
            if (name.equals("jessy") && i == 2) {
                try {
                    client.setData().forPath("/zk/leader", new String(name).getBytes());
                } catch (Exception e) {
                    log.error("leader is Cancel");
                }
            }
            String oldLeader = new String(client.getData().forPath("/zk/leader"));
            if (Strings.isNotBlank(oldLeader) && oldLeader.equals("jessy")) {
                System.out.println(name + " is leader");
                return;
            }

        }
    }
}
