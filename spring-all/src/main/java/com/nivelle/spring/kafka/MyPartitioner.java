package com.nivelle.spring.kafka;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2019/12/02
 */
public class MyPartitioner implements Partitioner {

    private Random random;

    @Override
    public void configure(Map<String, ?> configs) {

        random = new Random();
    }


    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitionInfoList = cluster.availablePartitionsForTopic(topic);
        int partitionCount = partitionInfoList.size();
        if (Objects.isNull(key)) {
            return 0;
        }
        String keyStr = key.toString();
        int lastPartitionInfoIndex = partitionCount - 1;
        System.err.println("PartitionInfoIndex is:" + lastPartitionInfoIndex);
        if (Strings.isBlank(keyStr) || !keyStr.contains("nivelle")) {
            return 0;
        }
        return lastPartitionInfoIndex;
    }

    @Override
    public void close() {
        return;
    }


}
