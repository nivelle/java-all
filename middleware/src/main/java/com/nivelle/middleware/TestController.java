package com.nivelle.middleware;

import com.google.common.collect.Maps;
import com.nivelle.middleware.pojo.ResponseResult;
import com.nivelle.middleware.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * mongotest
 *
 * @author fuxinzhong
 * @date 2020/02/26
 */
@RestController
@RequestMapping("/mongo")
public class TestController {

    private static final String CLOUD_TABLE_NAME = "nd_cloud_book";


    @Autowired
    MongoTemplate mongoTemplate;


    @RequestMapping("/add")
    public Object add() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("name", "nivelle");
        data.put("age", "28");
        data.put("_id", "111");
        User user = new User();
        user.setAge(1);
        user.setName("kk");
        data.put("user", user);
        Map result = mongoTemplate.save(data, CLOUD_TABLE_NAME);
        return ResponseResult.newResponseResult().setSuccess(result);
    }

    @RequestMapping("/find")
    public Object find() {
        Map result = mongoTemplate.findById("111", Map.class, CLOUD_TABLE_NAME);
        User user = (User) result.get("user");
        return ResponseResult.newResponseResult().setSuccess(user.getName());
    }


    @RequestMapping("/update")
    public Object update() {
        Query query = Query.query(Criteria.where("_id").is("111"));
        Update update = new Update();
        update.set("age", 29);
        update.set("name", null);
        update.set("user.age", 2);
        return ResponseResult.newResponseResult().setSuccess(mongoTemplate.updateFirst(query, update, CLOUD_TABLE_NAME));
    }
}
