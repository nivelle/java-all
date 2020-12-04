package com.nivelle.middleware.elastic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
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
    ElasticsearchRestTemplate elasticsearchTemplate;

    /**
     * http://127.0.0.1:9200/_cat/indices?v  //查看所有的索引
     */
    @RequestMapping("test")
    public String esTest() {
        elasticsearchTemplate.createIndex(Item.class);

        return "success";
    }
}
