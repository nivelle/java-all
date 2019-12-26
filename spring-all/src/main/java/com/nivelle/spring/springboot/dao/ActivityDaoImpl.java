package com.nivelle.spring.springboot.dao;

import com.nivelle.spring.springboot.entity.ActivityPvEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * JdbcTemplate
 *
 * @author fuxinzhong
 * @date 2019/08/03
 */
@Repository
public class ActivityDaoImpl {


    @Resource(name = "masterJdbcTemplate")
    private JdbcTemplate jdbcTemplate;


    private static final String SELECT_SQL = "select * from activity_pv where id=?";

    private static final String SELECTS_SQL = "select * from activity_pv where position_type=? and device_type=?";

    private static final String SELECT_FOR_UPDATE_SQL = "select * from activity_pv where id=? for update";

    private static final String UPDATE_SQL = "update activity_pv set activity_id=?,position_type=?,device_type=?,device_no=? where id =?";


    /**
     * queryForObject
     *
     * @return
     */
    public ActivityPvEntity getActivitiesById(long id) {
        try {
            Object params[] = new Object[]{id};
            /**
             * RowMapper<T
             */
            ActivityPvEntity activityPvEntity = jdbcTemplate.queryForObject(SELECT_SQL, params, new BeanPropertyRowMapper<>(ActivityPvEntity.class));
            return activityPvEntity;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * queryForList 原型
     *
     * @return
     */
    public List<Map<String, Object>> getActivityList() {
        try {
            Object params[] = new Object[]{3, 2};
            List<Map<String, Object>> activityPvEntities = jdbcTemplate.queryForList(SELECTS_SQL, params);
            return activityPvEntities;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * 对象映射原型
     *
     * @return
     */
    public List<ActivityPvEntity> getActivityList2() {
        try {
            Object params[] = new Object[]{3, 2};
            List<ActivityPvEntity> activityPvEntities = jdbcTemplate.query(SELECTS_SQL, params, new BeanPropertyRowMapper<>(ActivityPvEntity.class));
            return activityPvEntities;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * 自定义映射
     *
     * @return
     */
    public List<ActivityPvEntity> getActivityList3() {
        try {
            Object params[] = new Object[]{3, 2};
            List<ActivityPvEntity> activityPvEntities = jdbcTemplate.query(SELECTS_SQL, params, (resultSet, row) -> {

                Long id = resultSet.getLong("id");
                String activityId = resultSet.getString("activity_id");
                int positionType = resultSet.getInt("position_type");
                String ip = resultSet.getString("ip");
                String deviceType = resultSet.getString("device_type");
                String deviceNo = resultSet.getString("device_no");
                String createTime = resultSet.getString("create_time");
                String updateTime = resultSet.getString("update_time");
                ActivityPvEntity activityPvEntity = new ActivityPvEntity();
                activityPvEntity.setId(id.intValue());
                activityPvEntity.setDeviceType(deviceType);
                activityPvEntity.setActivityId(activityId);
                activityPvEntity.setPositionType(positionType);
                activityPvEntity.setIp(ip);
                activityPvEntity.setDeviceNo(deviceNo);
                activityPvEntity.setCreateTime(createTime);
                activityPvEntity.setUpdateTime(updateTime);
                return activityPvEntity;
            });
            return activityPvEntities;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * forUpdate
     *
     * @return
     */
    public ActivityPvEntity getActivitiesForUpdate(long id) {
        try {
            ActivityPvEntity activityPvEntity = jdbcTemplate.queryForObject(SELECT_FOR_UPDATE_SQL,
                    new Object[]{id}, new BeanPropertyRowMapper<>(ActivityPvEntity.class));
            return activityPvEntity;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * 更新
     *
     * @param activityPvEntity
     * @return
     */
    public int updateActivityPv(ActivityPvEntity activityPvEntity) {
        try {
            int changeCount = jdbcTemplate.update(UPDATE_SQL, new Object[]{activityPvEntity.getActivityId(),
                    activityPvEntity.getPositionType(), activityPvEntity.getDeviceType(), activityPvEntity.getDeviceNo(), activityPvEntity.getId()});
            return changeCount;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(e.getStackTrace());
        }
        return 0;
    }
}
