package com.nivelle.bigdata.clickhouse;

import com.nivelle.bigdata.clickhouse.mapper.UserTransactionInfoMapper;
import com.nivelle.bigdata.clickhouse.params.UserTransactionInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/03/10
 */
@Service
public class UserTransactionInfosService {

    @Autowired
    private UserTransactionInfoMapper userTransactionInfoMapper;

    public List<UserTransactionInfoResponse> getTransactionByCondition(HashMap<String, Object> params) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Object pageNo = params.get("pageNo");
        Object pageSize = params.get("pageSize");
        if (Objects.isNull(pageNo)) {
            params.put("pageNo", 1);
        }
        if (Objects.isNull(pageSize)) {
            params.put("pageSize", 100);
        }
        LocalDateTime localStartTime = LocalDateTime.parse(String.valueOf(params.get("startTime")), dtf);
        LocalDateTime localEndTime = LocalDateTime.parse(String.valueOf(params.get("endTime")), dtf);
        params.put("startTime", localStartTime);
        params.put("endTime", localEndTime);
        List<UserTransactionInfoResponse> result = userTransactionInfoMapper.getCondition(params);
        return result;

    }

}
