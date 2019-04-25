package com.nivelle.programming.springboot.controllor;

import com.nivelle.programming.springboot.entity.NdActivityPvEntity;
import com.nivelle.programming.springboot.mapper.NdActivityPvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("activity" )
public class ActivityPvController {

    @Autowired
    NdActivityPvMapper ndActivityPvMapper;

    @RequestMapping("/pvs" )
    public String hello() {

        List<NdActivityPvEntity> activities = ndActivityPvMapper.getAll();

        return activities.toString();
    }
}
