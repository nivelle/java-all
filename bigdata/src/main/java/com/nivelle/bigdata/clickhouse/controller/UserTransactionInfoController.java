package com.nivelle.bigdata.clickhouse.controller;

import com.nivelle.bigdata.clickhouse.entity.UserTransactionInfo;
import com.nivelle.bigdata.clickhouse.mapper.UserTransactionInfoMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author fuxinzhong
 * @date 2021/02/22
 */
@RestController
@RequestMapping("/transaction")
public class UserTransactionInfoController {
    @Resource
    private UserTransactionInfoMapper userTransactionInfoMapper;

    /**
     * 单个添加
     *
     * @return
     */
    @RequestMapping("/save")
    public String save() {
        int i = 0;
        UserTransactionInfo userTransactionInfo = new UserTransactionInfo();
        userTransactionInfo.setUserName("jessy");
        userTransactionInfo.setBookId(117 + i);
        userTransactionInfo.setBookName("千山万水");
        userTransactionInfo.setChapterId("201-1" + "-" + i);
        userTransactionInfo.setBookType(64);
        userTransactionInfo.setCategoryId(2 + i);
        userTransactionInfo.setCategoryName("男频");
        userTransactionInfo.setP2("123002");
        userTransactionInfo.setP16("ios");
        userTransactionInfo.setCopyrightId(125 + i);
        userTransactionInfo.setCompanyId("200251");
        userTransactionInfo.setCouponAmount(new BigDecimal("12.12"));
        userTransactionInfo.setAppleGiftAmount(new BigDecimal("10.00"));
        userTransactionInfo.setAppleAmount(new BigDecimal("0.98"));
        userTransactionInfo.setRefundAmount(new BigDecimal("9.87"));
        userTransactionInfo.setMainGiftAmount(new BigDecimal("45.09"));
        userTransactionInfo.setMainAmount(new BigDecimal("32.12"));
        userTransactionInfo.setAmount(new BigDecimal("100.98"));
        userTransactionInfo.setCreateTime(LocalDateTime.now());
        userTransactionInfoMapper.save(userTransactionInfo);
        return "sus";
    }

    /**
     * 单个添加
     *
     * @return
     */
    @RequestMapping("/batchSave")
    public String batchSave() {
        for (int i = 0; i < 100; i++) {
            UserTransactionInfo userTransactionInfo = new UserTransactionInfo();
            userTransactionInfo.setUserName("jessy");
            userTransactionInfo.setBookId(117 + i);
            userTransactionInfo.setBookName("千山万水");
            userTransactionInfo.setChapterId("201-1" + "-" + i);
            userTransactionInfo.setBookType(64);
            userTransactionInfo.setCategoryId(2 + i);
            userTransactionInfo.setCategoryName("男频");
            userTransactionInfo.setP2("123002");
            userTransactionInfo.setP16("ios");
            userTransactionInfo.setCopyrightId(125 + i);
            userTransactionInfo.setCompanyId("200251");
            userTransactionInfo.setCouponAmount(new BigDecimal("12.12"));
            userTransactionInfo.setAppleGiftAmount(new BigDecimal("10.00"));
            userTransactionInfo.setAppleAmount(new BigDecimal("0.98"));
            userTransactionInfo.setRefundAmount(new BigDecimal("9.87"));
            userTransactionInfo.setMainGiftAmount(new BigDecimal("45.09"));
            userTransactionInfo.setMainAmount(new BigDecimal("32.12"));
            userTransactionInfo.setAmount(new BigDecimal("100.98"));
            userTransactionInfo.setCreateTime(LocalDateTime.now());
            userTransactionInfoMapper.save(userTransactionInfo);
        }
        return "sus";
    }
}
