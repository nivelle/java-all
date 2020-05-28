package com.nivelle.middleware.kafka;

/**
 * kafka  broker 端参数
 *
 * @author fuxinzhong
 * @date 2020/05/27
 */
public class KafkaBrokerConfig {


    /**
     * 1. log.segment.bytes: 日志端文件最大大小，默认1GB
     *
     * 2. log.index.size.max.bytes: 索引文件最大大小，强制要求索引文件是索引项也就是8的整数倍。默认值10MB
     *
     * 3. log.retention.{hours|minutes|ms}:配置清除日志时间间隔
     *
     * 4. log.retention.bytes:默认值是-1,表示不会对log大小进行限制
     *
     */
}
