package com.nivelle.spring.springboot.service.impl;

import com.nivelle.spring.springboot.entity.ActivityPvEntity;
import com.nivelle.spring.springboot.mapper.ActivityPvMapper;
import com.nivelle.spring.springboot.service.ActivityService;
import com.nivelle.spring.springboot.service.ActivityTransactionService;
import com.nivelle.spring.springboot.entity.ActivityPvEntity;
import com.nivelle.spring.springboot.mapper.ActivityPvMapper;
import com.nivelle.spring.springboot.service.ActivityService;
import com.nivelle.spring.springboot.service.ActivityTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 事物方法，解决事物方法内部调用的问题
 *
 * @author fuxinzhong
 * @date 2019/08/10
 */
@Service
public class ActivityTransactionServiceImpl implements ActivityTransactionService {

    @Autowired
    private ActivityPvMapper activityPvMapper;

    @Autowired
    private ActivityService activityService;


    /**
     * 事物传播特性
     * <p>
     * PROPAGATION_REQUIRED--支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择。
     * PROPAGATION_SUPPORTS--支持当前事务，如果当前没有事务，就以非事务方式执行。
     * PROPAGATION_MANDATORY--支持当前事务，如果当前没有事务，就抛出异常。
     * PROPAGATION_REQUIRES_NEW--新建事务，如果当前存在事务，把当前事务挂起。
     * PROPAGATION_NOT_SUPPORTED--以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
     * PROPAGATION_NEVER--以非事务方式执行，如果当前存在事务，则抛出异常。
     */
    /**
     * 事物隔离级别
     *
     * 隔离级别在于处理多事务的并发问题。隔离级别在于处理多事务的并发问题。
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false, isolation = Isolation.READ_COMMITTED, timeout = 100, rollbackFor = Exception.class)
    public ActivityPvEntity getActivityInTransactional(long id) {
        ActivityPvEntity activityPvEntity = activityPvMapper.getActivityById(id);
        if (Objects.nonNull(activityPvEntity)) {
            activityService.requiredCommitted(activityPvEntity.getId());
        }
        //抛出一个非受检异常,即使requiredCommited事物方法已经提交,仍然会导致回滚.
        Object object = null;
        object.toString();
        return activityPvEntity;
    }
}
