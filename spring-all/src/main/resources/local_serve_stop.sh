#! /bin/bash

echo "stop zookeeper server......."
zkServer stop /usr/local/etc/zookeeper/zoo1.cfg
zkServer stop /usr/local/etc/zookeeper/zoo2.cfg
zkServer stop /usr/local/etc/zookeeper/zoo3.cfg

echo "stop zookeeper server ok ......."


## 关闭redis

echo "stop redis ......."

sudo pkill redis-server

echo "stop redis ok ......."


## mysql

echo "stop mysql ......."

sudo /usr/local/mysql/support-files/mysql.server stop

echo "stop mysql ok......."


## rabbitmq

echo "stop rabbimq ......."

sudo ./rabbitmqctl stop

echo "stop rabbimq ok ......."

## kafka

echo "stop Kafka ......."

kafka-server-stop /usr/local/etc/kafka/server.properties

echo "stop Kafka ok......."





