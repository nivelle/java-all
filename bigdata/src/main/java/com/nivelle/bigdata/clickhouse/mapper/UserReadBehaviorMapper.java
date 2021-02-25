package com.nivelle.bigdata.clickhouse.mapper;

import com.nivelle.bigdata.clickhouse.entity.UserReadBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/02/22
 */
@Mapper
public interface UserReadBehaviorMapper {
    // 写入数据
    void batchSave(@Param("userReadBehaviorList") List<UserReadBehavior> userReadBehaviorList);

    // 查询全部
    List<UserReadBehavior> selectList();

    void save(UserReadBehavior userReadBehavior);
}
