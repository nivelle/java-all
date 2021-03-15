package com.nivelle.bigdata.clickhouse.controller;

import com.nivelle.bigdata.clickhouse.UserTransactionInfosService;
import com.nivelle.bigdata.clickhouse.entity.UserTransactionInfo;
import com.nivelle.bigdata.clickhouse.mapper.UserTransactionInfoMapper;
import com.nivelle.bigdata.clickhouse.params.UserTransactionInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * @author fuxinzhong
 * @date 2021/02/22
 */
@RestController
@RequestMapping("/transaction")
public class UserTransactionInfoController {
    @Resource
    private UserTransactionInfoMapper userTransactionInfoMapper;

    @Autowired
    UserTransactionInfosService userTransactionInfosService;

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
        userTransactionInfo.setCategoryId1(2 + i);
        userTransactionInfo.setCategoryId2(2 + i);
        userTransactionInfo.setCategoryId3(24);
        userTransactionInfo.setCategoryId4(2 + i);
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
     * 批量添加
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
            userTransactionInfo.setCategoryId1(2 + i);
            userTransactionInfo.setCategoryId2(2 + i);
            userTransactionInfo.setCategoryId3(2 + i);
            userTransactionInfo.setCategoryId4(2 + i);
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

    /**
     * 批量添加
     *
     * @return
     */
    @RequestMapping("/selectList")
    public List<UserTransactionInfo> selectList() {

        return userTransactionInfoMapper.selectList();
    }


    /**
     * 批量添加
     *
     * @return
     */
    @PostMapping("/getCondition")
    public List<UserTransactionInfoResponse> getCondition(@RequestBody HashMap<String, Object> params) {

        System.out.println("查询参数：" + params);
        List<UserTransactionInfoResponse> result = userTransactionInfosService.getTransactionByCondition(params);

        return result;
    }
}
