package com.nivelle.bigdata.clickhouse.params;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户交易记录
 *
 * @author fuxinzhong
 * @date 2021/03/01
 */
@Data
public class UserTransactionInfoResponse {
    private int bookId;
    /**
     * 付费人数
     */
    private int payPersons;
    /**
     * 付费金额
     */
    private BigDecimal payAmount;
    /**
     * 付费充值币金额
     */
    private BigDecimal payRechargeAmount;
    /**
     * 付费赠送币金额
     */
    private BigDecimal payCouponAmount;
    /**
     * 付费退还币金额
     */
    private BigDecimal payRefundAmount;
}
