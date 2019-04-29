package com.nivelle.guide.springboot.controllor;

import com.nivelle.guide.springboot.entity.ActivityPvEntity;
import com.nivelle.guide.springboot.params.ActivityParams;
import com.nivelle.guide.springboot.pojo.vo.ResponseResult;
import com.nivelle.guide.springboot.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("activity")
@Validated
public class ActivityPvController {

    @Autowired
    ActivityService activityService;

    @RequestMapping("/pvs")
    @ResponseBody
    public Object hello() {

        List<ActivityPvEntity> activities = activityService.getAll();
        return activities;
    }

    @RequestMapping(value = "/pv", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult addPv(@Validated ActivityParams activityParams) {
        ActivityPvEntity activityPvEntity = new ActivityPvEntity();
        activityPvEntity.setActivityId(activityParams.getActivityId());
        activityPvEntity.setDeviceNo(activityParams.getDeviceNo());
        activityPvEntity.setDeviceType(activityParams.getDeviceType());
        activityPvEntity.setPositionType(activityParams.getPositionType());
        activityPvEntity.setIp(activityParams.getIp());
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

}
