package com.nivelle.guide.springboot.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Data
@Setter
@Getter
@ToString
public class SysRoleEntity {
    private Integer id; // 编号
    private String role; // 角色标识程序中判断使用,如"admin",这个是唯一的:
    private String description; // 角色描述,UI界面显示使用
    private Integer available = 0; // 是否可用,如果不可用将不会添加给用户
    //角色 -- 权限关系：多对多关系;
    private List<SysPermissionEntity> permissions;
    private String createTime;
    private String updateTime;

}