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
docker run -it --rm --link clickhouse-test-server:clickhouse-server yandex/clickhouse-client --host clickhouse-server -e "LIBHDFS3_CONF=/etc/clickhouse-server/hdfs-client.xml"

````

