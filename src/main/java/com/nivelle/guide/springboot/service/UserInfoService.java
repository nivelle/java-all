package com.nivelle.guide.springboot.service;


import com.nivelle.guide.springboot.entity.UserInfoEntity;

public interface UserInfoService {
    /**
     * 通过username查找用户信息;
     */
    UserInfoEntity findByUsername(String username);
}