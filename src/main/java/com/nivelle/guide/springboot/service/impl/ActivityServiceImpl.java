package com.nivelle.guide.springboot.service.impl;

import com.nivelle.guide.springboot.entity.ActivityPvEntity;
import com.nivelle.guide.springboot.mapper.ActivityPvMapper;
import com.nivelle.guide.springboot.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityPvMapper activityPvMapper;

    public List<ActivityPvEntity> getAll() {
        List<ActivityPvEntity> ativities = activityPvMapper.getAll();
        return ativities;

    }

    public int insert(ActivityPvEntity activityPvEntity) {

        return activityPvMapper.insert(activityPvEntity);
    }

    public int update(ActivityPvEntity activityPvEntity) {
        return 0;
    }

}
