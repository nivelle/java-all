package com.nivelle.spring.springboot.service.impl;

import com.nivelle.spring.springboot.dao.ActivityDaoImpl;
import com.nivelle.spring.pojo.ActivityPvEntity;
import com.nivelle.spring.springboot.mapper.ActivityPvMapper;
import com.nivelle.spring.springboot.service.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * mybatis && jdbcTemplate
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);


    @Autowired
    private ActivityDaoImpl activityDaoImpl;


    @Override
    public int update(ActivityPvEntity activityPvEntity) {
        return 0;
    }


    /**
     * 1.只有 public 方法打上 @Transactional 注解, 事务控制才能生效.
     * 2.注意自调用问题, @Transactional 注解仅在外部类的调用才生效, 原因是使用 Spring AOP 机制造成的. 所以: 主调函数如果是本Service类, 应该也要打上 @Transactional, 否则事务控制被忽略.
     * 3.缺省的情况下, 只有 RuntimeException 类异常才会触发回滚. 如果在事务中抛出其他异常,并期望回滚事务, 必须设定 rollbackFor 参数.
     * 4.如果主调函数和多个被调函数都加了 @Transactional 注解, 则整个主调函数将是一个统一的事务控制范围, 甚至它们分属多个Service也能被统一事务控制着
     * 5.通常我们应该使用 Propagation.REQUIRED, 但需要说明的是, 如果一个非事务方法顺序调用了"两个不同service bean"的事务函数, 它们并不在同一个事务上下文中, 而是分属于不同的事务上下文.
     */

    /**
     * 1. public限制和自调用问题都是因为使用Spring AOP代理造成的，如果要解决需要使用AspectJ取代Spring AOP代理
     * 2. Transactional 也可以放在类上,此时它的所有方法和子类都能识别
     */
    @Override
    //不加事物控制，则会更新成功;否则依然能更新成功
    //@Transactional(propagation = Propagation.REQUIRED, readOnly = false, isolation = Isolation.READ_COMMITTED, timeout = 100, rollbackFor = Exception.class)
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = false, isolation = Isolation.READ_COMMITTED, timeout = 100, rollbackFor = Exception.class)
    public int requiredCommitted(long id) {
        ActivityPvEntity activityPvEntity = activityDaoImpl.getActivitiesById(id);
        if (Objects.nonNull(activityPvEntity)) {
            activityPvEntity.setActivityId("渣哥");
            int result = activityDaoImpl.updateActivityPv(activityPvEntity);
            return result;
        }
        return 0;
    }

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

    public ActivityPvEntity getActivityInTransactional(long id) {
        ActivityPvEntity activityPvEntity = activityDaoImpl.getActivitiesById(id);
        if (Objects.nonNull(activityPvEntity)) {
            //此时事物不起作用,因为调用者和被调用者同属一个类
            //this.requiredCommited(activityPvEntity.getId());

            //通过使用代理调用内部事物方法让事物传播特性生效
            ActivityService activityService = (ActivityService) AopContext.currentProxy();
            activityService.requiredCommitted(activityPvEntity.getId());

        }
        //抛出一个非受检异常
        this.throwAException();
        return activityPvEntity;
    }


    /**
     * 制造一个非受检异常
     * 1. Error 和 RuntimeException 以及他们的子类
     * 2. 编译时不会提示和发现这些异常，不要求coder处理这些异常，但是也可以有意识的去处理，但这种问题根本的解决方式时修改代码
     * 3. ArithmeticException;ArrayIndexOutOfBoundsException;NullPointerException
     */
    private void throwAException() {
        int a = 10;
        int c = a / 0;
        return;
    }

    private void throwANullException() {
        Object object = null;
        object.toString();
        return;
    }

}
