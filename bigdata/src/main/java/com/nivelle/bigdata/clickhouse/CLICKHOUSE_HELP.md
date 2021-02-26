### hadoop 启动

````
docker-compose up -d
````

### hadoop 管理后台

````
http://39.105.201.242:50070/dfshealth.html#tab-datanode
````

### clickhouse 启动

````
docker run -it --rm --link clickhouse-test-server:clickhouse-server yandex/clickhouse-client --host clickhouse-server -e "LIBHDFS3_CONF=/etc/clickhouse-server/hdfs-client.xml
````

### clickhouse创建 hdfs引擎的表

````
create table hdfsTest14(name String) 
engine = HDFS('hdfs://39.105.201.242:8020/hdfs/*','CSV');
````

#### clickhouse本地启动

````
docker run -d --name clickhouse-server --ulimit nofile=262144:262144 -p 8123:8123 yandex/clickhouse-server
````
#### 库语句创建

````
CREATE DATABASE IF NOT EXISTS user_info;
````

#### 表语句创建

``````
CREATE TABLE user_info.cs_user_info 
(
    id UInt16,
    user_name String,
    pass_word String,
    phone String,
    email String,
    create_day date
)ENGINE = MergeTree partition by toYYYYMMDD(create_day) primary key (id) order by (id);

``````

#### mergeTree合并树表引擎参数

- partition by： 分区键，用于指定表数据以什么标准分区，可以是单列也可以是组合列，默认值为all;合理使用可以减少查询时数据文件的扫描范围

- order by:排序键，用于指定在一个数据片段内，数据排序标准；可以是单列，也可以是组合列

- primary key: 主键，声明后会依据主键生成一级索引，用于加速表查询；默认情况下，主键和排序键相同，一般情况下，单个数据片段内，数据与一级索引以相同的规则升序排列；mergeTree主键允许存在重复数据

- sample by: 抽样表达式，用于声明数据以何种标准进行采样。如果使用了此配置，主键的配置中也需要声明同样的表达式子

- settings :index_granularity : 表示索引的粒度，默认值为8192。 表示索引在默认情况下，每间隔8192行数据才生成一条索引。

- settings: index_granularity_bytes:自适应间隔大小特性，根据每一批次写入数据的体量大小，动态划分间隔大小。


