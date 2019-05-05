package com.nivelle.guide.springboot.service.impl;

import com.nivelle.guide.springboot.entity.UserInfoEntity;
import com.nivelle.guide.springboot.mapper.SysUserInfoMapper;
import com.nivelle.guide.springboot.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {


    private static final Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    @Autowired
    private SysUserInfoMapper sysUserInfoMapper;


    @Override
    public UserInfoEntity findByUsername(String name) {
        logger.info("查询用户信息，name={}",name);
        return sysUserInfoMapper.getUserInfoByName(name);
    }
}