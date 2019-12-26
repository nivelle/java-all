package com.nivelle.spring.springboot.service.impl;

import com.nivelle.spring.springboot.entity.*;
import com.nivelle.spring.springboot.mapper.*;
import com.nivelle.spring.springboot.service.ShiroSysService;
import com.nivelle.spring.springboot.entity.*;
import com.nivelle.spring.springboot.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiroSysServiceImpl implements ShiroSysService {


    private static final Logger logger = LoggerFactory.getLogger(ShiroSysServiceImpl.class);

    @Autowired
    private SysUserInfoMapper sysUserInfoMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;
    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;


    @Override
    public UserInfoEntity findByUsername(String name) {
        logger.info("查询用户信息，name={}", name);
        return sysUserInfoMapper.getUserInfoByName(name);
    }

    @Override
    public List<SystemUserRoleEntity> getSysUserRoleListByUid(long uid) {

        logger.info("查询用户角色信息，uid={}", uid);

        return sysUserRoleMapper.getSytemUserRoleByUid(uid);

    }

    @Override
    public SysRoleEntity getSysRoleEntityById(long id) {

        logger.info("具体查询用户角色信息，id={}", id);

        return sysRoleMapper.getSysRoleEntityById(id);

    }

    @Override
    public List<SysRolePermissionEntity> getSysRolePermissionEntityByRoleId(long roleId) {

        logger.info("查询用户权限信息,roleId={}", roleId);

        return sysRolePermissionMapper.getPermissionByRoleId(roleId);

    }

    @Override
    public SysPermissionEntity getSysPermissionEntityById(long id) {

        logger.info("具体查询用户权限信息，id={}", id);

        return sysPermissionMapper.getSysPermissionById(id);

    }


}