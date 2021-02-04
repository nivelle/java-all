package com.nivelle.bigdata.clickhouse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.json.JSONObject;

import java.util.List;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/02/04
 */
@SpringBootTest
public class SemTest {

    @Test
    public void getFrsDataTest() {
        System.out.println("******************");
        String sql = "select * from marketing.sem_campaign_real_time_report";
        List<JSONObject> result = ClickHouseUtil.exeSql(sql);
        System.out.println("******************");
    }
}


