package com.nivelle.guide.springboot.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
public class SysPermissionEntity {

    private Long id;
    private Integer available;
    private String name;
    private String parentId;
    private String parentIds;
    private String resourceType;
    private String url;
    private String createTime;
    private String updateTime;

}
