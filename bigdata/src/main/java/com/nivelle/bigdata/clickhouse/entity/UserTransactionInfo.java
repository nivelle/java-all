package com.nivelle.bigdata.clickhouse.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/03/01
 */
@Data
public class UserTransactionInfo {

    private String userName;
    private int bookId;
    private String bookName;
    /**
     * 章节ID
     **/
    private String chapterId;
    /**
     * 24 书籍、26 听书，27大咖
     **/
    private int bookType;

    /**
     * 书籍底层分类id
     **/
    private int categoryId1;
    /**
     * 书籍底层分类id
     **/
    private int categoryId2;
    private int categoryId3;
    private int categoryId4;

    /**
     * 渠道
     **/
    private String p2;
    /**
     * 机型
     **/
    private String p16;

    /**
     * 版权方ID
     **/
    private int copyrightId;
    /**
     * 企业ID
     **/
    private String companyId;
    /**
     * 交易来源
     **/
    private String origin;

    /**
     * 赠送币金额
     **/
    private BigDecimal couponAmount;

    /**
     * 苹果礼品账户交易金额
     **/

    private BigDecimal appleGiftAmount;
    /**
     * 苹果账户交易金额
     **/
    private BigDecimal appleAmount;
    /**
     * 退款账户交易金额
     **/
    private BigDecimal refundAmount;
    /**
     * 礼品账户交易金额
     **/
    private BigDecimal mainGiftAmount;
    /**
     * 主账户交易金额
     **/
    private BigDecimal mainAmount;
    /**
     * 交易总金额
     **/
    private BigDecimal amount;
    /**
     * 创建时间
     **/
    private LocalDateTime createTime;


}
