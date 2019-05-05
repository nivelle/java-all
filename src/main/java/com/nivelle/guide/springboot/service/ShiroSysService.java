package com.nivelle.guide.springboot.service;


import com.nivelle.guide.springboot.entity.*;

import java.util.List;

public interface ShiroSysService {
    /**
     * 通过username查找用户信息;
     */
    UserInfoEntity findByUsername(String name);


    List<SystemUserRoleEntity> getSysUserRoleListByUid(long uid);


    SysRoleEntity getSysRoleEntityById(long id);


    List<SysRolePermissionEntity> getSysRolePermissionEntityByRoleId(long roleId);


    SysPermissionEntity getSysPermissionEntityById(long id);


}