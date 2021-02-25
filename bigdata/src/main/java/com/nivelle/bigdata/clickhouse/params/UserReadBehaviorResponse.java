package com.nivelle.bigdata.clickhouse.params;

import lombok.Data;


/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/02/25
 */
@Data
public class UserReadBehaviorResponse {
    private int bookId;
    private long uv;
    private long pv;
    private long sumTimes;
}
