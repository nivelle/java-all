package com.nivelle.bigdata.clickhouse.mapper;

import com.nivelle.bigdata.clickhouse.entity.UserTransactionInfo;
import com.nivelle.bigdata.clickhouse.params.UserTransactionInfoResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * 交易记录
 *
 * @author fuxinzhong
 * @date 2021/02/22
 */
@Mapper
public interface UserTransactionInfoMapper {

    // 写入数据
    void batchSave(@Param("userTransactionInfoList") List<UserTransactionInfo> userTransactionInfoList);

    void save(UserTransactionInfo userTransactionInfo);

    // 查询全部
    List<UserTransactionInfo> selectList();

    List<UserTransactionInfoResponse> getCondition(HashMap<String,Object> params);


}
