package com.nivelle.guide.springboot.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@Getter
@ToString
public class SysRoleEntity {

    private Long id;
    private Integer available;
    private String description;
    private String role;
    private String createTime;
    private String updateTime;

}
