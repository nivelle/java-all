package com.nivelle.spring.springboot.service;

import com.nivelle.spring.springboot.entity.ActivityPvEntity;
import com.nivelle.spring.springboot.entity.ActivityPvEntity;

/**
 * Spring AOP事物代理默认只能代理接口
 *
 * @author fuxinzhong
 * @date 2019/08/10
 */
public interface ActivityTransactionService {

    ActivityPvEntity getActivityInTransactional(long id);
}
