package com.nivelle.guide.springboot.controllor;

import com.nivelle.guide.springboot.entity.NdActivityPvEntity;
import com.nivelle.guide.springboot.mapper.NdActivityPvMapper;
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
