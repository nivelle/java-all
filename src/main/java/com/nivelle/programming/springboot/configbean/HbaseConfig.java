package com.nivelle.programming.springboot.configbean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.hbase.HbaseTemplate;


@Configuration
public class HbaseConfig {
//    @Value("${hbase.zookeeper.quorum}")
//    private String zookeeperQuorum;
//
//    @Value("${hbase.zookeeper.property.clientPort}")
//    private String clientPort;
//
//    @Value("${hbase.zookeeper.znode.parent}")
//    private String znodeParent;
//
//    @Bean
//    public HbaseTemplate hbaseTemplate() {
//        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
//        conf.set("hbase.zookeeper.quorum", zookeeperQuorum);
//        conf.set("hbase.zookeeper.property.clientPort", clientPort);
//        conf.set("hbase.zookeeper.znode.paren", znodeParent);
//        return new HbaseTemplate(conf);
//    }


}

