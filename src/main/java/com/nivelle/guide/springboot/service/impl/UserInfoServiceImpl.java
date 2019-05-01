package com.nivelle.guide.springboot.service.impl;

import com.nivelle.guide.springboot.entity.UserInfoEntity;
import com.nivelle.guide.springboot.mapper.SysUserInfoMapper;
import com.nivelle.guide.springboot.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    private SysUserInfoMapper sysUserInfoMapper;


    @Override
    public UserInfoEntity findByUsername(String username) {
        System.out.println("UserInfoServiceImpl.findByUsername()");
        return sysUserInfoMapper.getUserInfoByName(username);
    }
}