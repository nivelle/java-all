package com.nivelle.spring.configbean;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:config/application.properties")
public class MyHBaseConfiguration {

    @Value("${hbase.client.scanner.caching}")
    private String scannerCachingSize;

    @Value("${hbase.client.operation.timeout}")
    private String operationTimeout;

    @Value("${hbase.client.scanner.timeout.period}")
    private String scanTimeout;

    @Value("${hbase.rpc.timeout}")
    private String rpcTimeout;

    @Value("${hbase.client.retries.number}")
    private String retryNum;

    @Value("${hbase.client.pause}")
    private String clientPause;

    @Value("${hbase.client.ipc.pool.size}")
    private String poolSize;

    @Value("${hbase.client.ipc.pool.type}")
    private String poolType;

    @Bean
    public org.apache.hadoop.conf.Configuration configuration() {
        org.apache.hadoop.conf.Configuration configuration = HBaseConfiguration.create();
        configuration.set(HConstants.ZOOKEEPER_QUORUM, "127.0.0.1");
        configuration.set(HConstants.ZOOKEEPER_CLIENT_PORT, "2181");
        configuration.set(HConstants.HBASE_CLIENT_SCANNER_CACHING, scannerCachingSize);
        configuration.set(HConstants.HBASE_CLIENT_OPERATION_TIMEOUT, operationTimeout);
        configuration.set(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, scanTimeout);
        configuration.set(HConstants.HBASE_RPC_TIMEOUT_KEY, rpcTimeout);
        configuration.set(HConstants.HBASE_CLIENT_RETRIES_NUMBER, retryNum);
        configuration.set(HConstants.HBASE_CLIENT_PAUSE, clientPause);
        configuration.set(HConstants.HBASE_CLIENT_IPC_POOL_TYPE, poolType);
        configuration.set(HConstants.HBASE_CLIENT_IPC_POOL_SIZE, poolSize);
        return configuration;
    }


    @Bean
    public HbaseTemplate myHBaseTemplate(org.apache.hadoop.conf.Configuration configuration) {
        return new HbaseTemplate(configuration);
    }

}
