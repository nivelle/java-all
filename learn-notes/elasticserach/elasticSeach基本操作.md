### 查看集群中的索引

```
method:get
http://localhost:9200/_cat/indices?v

health status index uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   item  Rmn1v6R1QxCLvM_Emvl1Bw   5   1          0            0      1.2kb          1.2kb
```

### 创建索引

##### 索引名字为product
```
method: put

localhost:9200/product?pretty
```

##### 返回值
```
{
    "acknowledged": true,
    "shards_acknowledged": true,
    "index": "product"
}

```

### 删除索引

```
method: delete 

localhost:9200/product?pretty

```

##### 返回值

```
{
    "acknowledged": true
}

```

### 新增文档并创建索引

```

localhost:9200/shoes/product/1

shoes 指索引名，product 指索引的类型，id是这条数据的id

```


##### 入参：

```
method: put

localhost:9200/shoes/product/1

{
    "name":"NB鞋子",
    "desc":"很好的鞋子",
    "price":530,
    "producer":"NB producer",
    "tags":["实用","美观"]
}

```

##### 返回值

```
{
    "_index": "shoes",
    "_type": "product",
    "_id": "1",
    "_version": 1,
    "result": "created",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 0,
    "_primary_term": 1
}

```

### 查询文档

#### 入参

```
method: get

localhost:9200/shoes/product/1

```

##### 返回值

````

{
    "_index": "shoes",
    "_type": "product",
    "_id": "1",
    "_version": 1,
    "_seq_no": 0,
    "_primary_term": 1,
    "found": true,
    "_source": {
        "name": "NB鞋子",
        "desc": "很好的鞋子",
        "price": 530,
        "producer": "NB producer",
        "tags": [
            "实用",
            "美观"
        ]
    }
}
````

### 文档修改

##### 入参

```
localhost:9200/shoes/product/1/_update

{
    "doc":{
        "name":"nb 最新款222222"
    }
}

```

##### 返回值

```
{
    "_index": "shoes",
    "_type": "product",
    "_id": "1",
    "_version": 3,
    "result": "updated",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 2,
    "_primary_term": 1
}

```

### 文档查询

##### 入参

```
method: get

localhost:9200/shoes/product/_search

{
    "query":{"match_all":{}}
}

```

##### 返回值

```

{
    "took": 14, //耗费了几毫秒
    "timed_out": false,
    "_shards": { //数据拆成了5个分片，所以对于搜索请求，会打到所有的primary shard（或者是它的某个replica shard也可以）
        "total": 5,
        "successful": 5,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": 1, //查询结果的数量，1个document
        "max_score": 1.0, //score的含义，就是document对于一个search的相关度的匹配分数，越相关，就越匹配，分数也高
        "hits": [ //包含了匹配搜索的document的详细数据
            {
                "_index": "shoes",
                "_type": "product",
                "_id": "1",
                "_score": 1.0,
                "_source": {
                    "name": "nb 最新款222222",
                    "desc": "很好的鞋子",
                    "price": 530,
                    "producer": "NB producer",
                    "tags": [
                        "实用",
                        "美观"
                    ]
                }
            }
        ]
    }
}

```

### 条件查询


##### 入参

```
{
    "query":{"match":{"name":"NB"}}
}


```

##### 返回值
```
{
    "took": 46,
    "timed_out": false,
    "_shards": {
        "total": 5,
        "successful": 5,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": 1,
        "max_score": 0.2876821,
        "hits": [
            {
                "_index": "shoes",
                "_type": "product",
                "_id": "1",
                "_score": 0.2876821,
                "_source": {
                    "name": "nb 最新款222222",
                    "desc": "很好的鞋子",
                    "price": 530,
                    "producer": "NB producer",
                    "tags": [
                        "实用",
                        "美观"
                    ]
                }
            }
        ]
    }
}

```

### 分页查询

#### 入参

```
{
    "query":{"match_all":{}},
    "from":1,
    "size":2

}

```

#### 返回值

```
{
    "took": 113,
    "timed_out": false,
    "_shards": {
        "total": 5,
        "successful": 5,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": 3,
        "max_score": 1.0,
        "hits": [
            {
                "_index": "shoes",
                "_type": "product",
                "_id": "1",
                "_score": 1.0,
                "_source": {
                    "name": "nb 最新款222222",
                    "desc": "很好的鞋子",
                    "price": 530,
                    "producer": "NB producer",
                    "tags": [
                        "实用",
                        "美观"
                    ]
                }
            },
            {
                "_index": "shoes",
                "_type": "product",
                "_id": "3",
                "_score": 1.0,
                "_source": {
                    "name": "NB鞋子",
                    "desc": "很好的鞋子",
                    "price": 530,
                    "producer": "NB producer",
                    "tags": [
                        "实用",
                        "美观"
                    ]
                }
            }
        ]
    }
}

```

### 查询指定字段

##### 入参

```
localhost:9200/shoes/product/_search

{
    "query":{"match_all":{}},
    "_source":["name","price"]
}

```

##### 返回值

```
{
    "took": 231,
    "timed_out": false,
    "_shards": {
        "total": 5,
        "successful": 5,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": 3,
        "max_score": 1.0,
        "hits": [
            {
                "_index": "shoes",
                "_type": "product",
                "_id": "2",
                "_score": 1.0,
                "_source": {
                    "price": 530,
                    "name": "NB鞋子"
                }
            },
            {
                "_index": "shoes",
                "_type": "product",
                "_id": "1",
                "_score": 1.0,
                "_source": {
                    "price": 530,
                    "name": "nb 最新款222222"
                }
            },
            {
                "_index": "shoes",
                "_type": "product",
                "_id": "3",
                "_score": 1.0,
                "_source": {
                    "price": 530,
                    "name": "NB鞋子"
                }
            }
        ]
    }
}
```

### 复杂查询

#### 入参

```


```

























