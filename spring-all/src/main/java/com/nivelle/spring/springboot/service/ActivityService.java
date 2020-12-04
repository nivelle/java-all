package com.nivelle.spring.springboot.service;

import com.nivelle.spring.pojo.ActivityPvEntity;

import java.util.List;

public interface ActivityService {

    int update(ActivityPvEntity activityPvEntity);


    int requiredCommitted(long id);

    ActivityPvEntity getActivityInTransactional(long id);

}
