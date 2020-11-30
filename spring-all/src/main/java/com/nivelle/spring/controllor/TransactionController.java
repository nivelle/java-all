package com.nivelle.spring.controllor;

import com.nivelle.spring.pojo.ActivityPvEntity;
import com.nivelle.spring.springboot.service.ActivityService;
import com.nivelle.spring.springboot.service.ActivityTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 事物测试
 *
 * @author nivelle
 * @date 2019/08/09
 */
@Controller
@RequestMapping("test/transaction")
@Validated
public class TransactionController {

    @Autowired
    ActivityService activityService;
    @Autowired
    ActivityTransactionService activityTransactionService;


    @RequestMapping("/requiredCommitted/{id}")
    @ResponseBody
    public Object forUpdate(@PathVariable String id) {
        int result = activityService.requiredCommitted(Long.valueOf(id));
        return result;
    }

    /**
     * 非事物方法调用了一个Propagation.REQUIRED 级别的事物方法，则非事物方法也会被分配一个事物
     *
     * @param id
     * @return
     */
    @RequestMapping("/requiredTransaction/{id}")
    @ResponseBody
    public Object requiredTransaction(@PathVariable String id) {
        ActivityPvEntity activityPvEntity = activityTransactionService.getActivityInTransactional(Long.valueOf(id));
        return activityPvEntity;
    }

}
