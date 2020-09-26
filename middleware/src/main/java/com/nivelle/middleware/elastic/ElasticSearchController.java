package com.nivelle.middleware.elastic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ElasticSearch
 *
 * @author fuxinzhong
 * @date 2020/09/26
 */
@RestController
@RequestMapping("/es")
public class ElasticSearchController {


    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @RequestMapping("test")
    public String esTest() {
        elasticsearchTemplate.createIndex(Item.class);

        return "success";
    }
}
