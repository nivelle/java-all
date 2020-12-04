package com.nivelle.spring;

import com.nivelle.spring.springboot.dao.ActivityDaoImpl;
import com.nivelle.spring.pojo.ActivityPvEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2019/08/03
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class JdbcTemplateTest {


    @Autowired
    ActivityDaoImpl activityDao;

    @Test
    public void testForUpdate() {
        ActivityPvEntity activityPvEntity = activityDao.getActivitiesForUpdate(1);
        System.out.println(activityPvEntity);
        int changeCount = activityDao.updateActivityPv(activityPvEntity);
        System.out.println("更新结果:" + changeCount);
    }
}
