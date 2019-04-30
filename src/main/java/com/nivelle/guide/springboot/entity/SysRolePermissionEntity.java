package com.nivelle.guide.springboot.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
public class SysRolePermissionEntity {

    private Long id;
    private Long roleId;
    private Long permissionId;
    private String createTime;
    private String updateTime;


}
