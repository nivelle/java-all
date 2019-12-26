package com.nivelle.spring.springboot.mapper;

import com.nivelle.spring.springboot.entity.SysRolePermissionEntity;
import com.nivelle.spring.springboot.entity.SysRolePermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper//可以使用@Mapper注解，但是每个类加注解较麻烦，所以统一配置@MapperScan在application启动类中
@Repository
public interface SysRolePermissionMapper {


    List<SysRolePermissionEntity> getPermissionByRoleId(long roleId);

}
