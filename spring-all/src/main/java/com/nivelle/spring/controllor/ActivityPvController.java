package com.nivelle.spring.controllor;

import com.nivelle.spring.pojo.ResponseResult;
import com.nivelle.spring.pojo.ActivityPvEntity;
import com.nivelle.spring.pojo.ActivityParams;
import com.nivelle.spring.springboot.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("test/activity")
@Validated
public class ActivityPvController {

    @Autowired
    ActivityService activityService;

    @RequestMapping(value = "/pvs",produces = "application/json")
    @ResponseBody
    public Object hello() {

        List<ActivityPvEntity> activities = activityService.getAll();
        return activities;
    }

    @RequestMapping(value = "/pv", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult addPv(@Validated ActivityParams activityParams, @RequestParam String deviceNo) {
        ActivityPvEntity activityPvEntity = new ActivityPvEntity();
        activityPvEntity.setActivityId(activityParams.getActivityId());
        activityPvEntity.setDeviceNo(activityParams.getDeviceNo());
        activityPvEntity.setDeviceType(activityParams.getDeviceType());
        activityPvEntity.setPositionType(activityParams.getPositionType());
        activityPvEntity.setIp(activityParams.getIp());
        System.out.println("deviceNo 直接接收:" + deviceNo);
        System.out.println("deviceNo 封装接收:" + activityParams.getDeviceNo());
        int result = activityService.insert(activityPvEntity);
        if (result > 0) {
            /**使用 useGeneratedKeys="true" keyProperty="id" 使得插入操作返回的是主键,而不是影响行数
             * 而且影响行数已经被映射到插入类中,需要重新获取,直接返回的值仍然是影响行数。
             */
            return ResponseResult.newResponseResult().setSuccess(activityPvEntity.getId());
        } else {
            return ResponseResult.newResponseResult().setFail(-1, "insert fail");
        }

    }


    @RequestMapping(value = "pv/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult getPv(@PathVariable("id") @Validated Integer id) {

        ActivityPvEntity activityPvEntity = activityService.getActivityById(id);
        if (Objects.nonNull(activityPvEntity)) {
            return ResponseResult.newResponseResult().setSuccess(activityPvEntity);
        } else {
            return ResponseResult.newResponseResult().setFail(-1, "activityPv is null");
        }
    }



}
