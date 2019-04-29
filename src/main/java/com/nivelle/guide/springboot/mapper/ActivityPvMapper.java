package com.nivelle.guide.springboot.mapper;

import com.nivelle.guide.springboot.entity.ActivityPvEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Mapper//可以使用@Mapper注解，但是每个类加注解较麻烦，所以统一配置@MapperScan在application启动类中
@Repository
public interface ActivityPvMapper {

    Optional<List<ActivityPvEntity>> getAll();

    int insert(ActivityPvEntity activityPvEntity);

    int update(ActivityPvEntity activityPvEntity);


}
