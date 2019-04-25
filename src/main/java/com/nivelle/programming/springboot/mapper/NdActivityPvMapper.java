package com.nivelle.programming.springboot.mapper;

import com.nivelle.programming.springboot.entity.NdActivityPvEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;



@Mapper//可以使用@Mapper注解，但是每个类加注解较麻烦，所以统一配置@MapperScan在application启动类中
@Repository
public interface NdActivityPvMapper {

    List<NdActivityPvEntity> getAll();

    int insert(NdActivityPvEntity ndActivityPvEntity);

    int update(NdActivityPvEntity ndActivityPvEntity);


}
