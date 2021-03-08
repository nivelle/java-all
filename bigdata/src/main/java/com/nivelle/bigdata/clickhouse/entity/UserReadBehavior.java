package com.nivelle.bigdata.clickhouse.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户阅读行为
 *
 * @author fuxinzhong
 * @date 2021/02/25
 */
@Data
public class UserReadBehavior {

    private String userName;
    private int bookId;
    /**
     * 章节ID
     **/
    private String chapterId;
    /**
     * 24 书籍、26 听书，27大咖
     **/
    private int bookType;
    /**
     * 阅读时长
     **/
    private int readTimes;
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
     * 用户Ip地址
     **/
    private String ip;
    /**
     * 版权方ID
     **/
    private int copyrightId;
    /**
     * 企业ID
     **/
    private String companyId;
    /**
     * 1:在企业套餐里 2:不在企业套餐里
     **/
    private int inCoPkg;
    /**
     * 用户分组ID
     **/
    private int userGroupId;
    /**
     * 创建时间
     **/
    private LocalDateTime createTime;

}
