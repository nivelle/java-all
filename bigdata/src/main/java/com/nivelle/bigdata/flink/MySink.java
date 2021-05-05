package com.nivelle.bigdata.flink;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/05/05
 */
@Slf4j
public class MySink extends RichSinkFunction<String> {

    private AnnotationConfigApplicationContext ctx;

    public MySink(){
        log.info("MySink new");
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        this.ctx = new AnnotationConfigApplicationContext(MySink.class);

        log.info("MySink open");

        // 这里获取了配置的数据源
        DataSource ds = ctx.getBean(DataSource.class);
        log.info("----------test info------------{}",ds);
    }

    @Override
    public void invoke(String value, Context context) throws Exception {
        //
        log.info(value);
    }

    @Override
    public void close() throws Exception {
        // 关闭容器
        ctx.close();
        log.info("MySink close");
    }
}

