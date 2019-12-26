package com.nivelle.spring.springboot.mapper;

import com.nivelle.spring.springboot.entity.SysRoleEntity;
import com.nivelle.spring.springboot.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper//可以使用@Mapper注解，但是每个类加注解较麻烦，所以统一配置@MapperScan在application启动类中
@Repository
public interface SysRoleMapper {


    SysRoleEntity getSysRoleEntityById(long id);

}
