package com.nivelle.spring.springboot.mapper;

import com.nivelle.spring.pojo.ActivityPvEntity;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;


@Mapper//可以使用@Mapper注解，但是每个类加注解较麻烦，所以统一配置@MapperScan在application启动类中
@Repository
public interface ActivityPvMapper {
    /**
     * XML中只需resultType属性值为实体对象别名或全路径名。
     * mybatis会通过接口文件的返回值类型来判断返回的是集合还是对象。
     * 如果是对象，则按常规查询并返回；如果是List集合，mybatis则会将查询到的多条记录设置进集合中并返回
     *
     * @return
     */
    List<ActivityPvEntity> getAll();

    int insert(ActivityPvEntity activityPvEntity);

    int update(ActivityPvEntity activityPvEntity);

    ActivityPvEntity getActivityById(long id);


}
