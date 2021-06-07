#! /bin/bash

## zookeeper

echo "start zookeeper server1......."

zkServer start /usr/local/etc/zookeeper/zoo1.cfg

echo "end zookeeper server1 ok......."

echo "start zookeeper server2......."

zkServer start /usr/local/etc/zookeeper/zoo2.cfg

echo "end zookeeper server2 ok ......."

echo "start zookeeper server3 ......."

zkServer start /usr/local/etc/zookeeper/zoo3.cfg

echo "start zookeeper server3 ok....."

## 启动redis

echo "start redis ......."

redis-server

echo "start redis ok ......."

## 启动rabbitmq

echo "start rabbimq ......."

rabbitmq-server

echo "start rabbimq ok......."

## kafka

echo "start kafka ......."

kafka-server-start /usr/local/etc/kafka/server.properties

echo "start kafka ok ......."

echo "start hbase ......."

start-hbase.sh

echo "start hbase ok ......."


sudo /usr/local/mysql/support-files/mysql.server start

echo "start mysql ok ......."


echo "start all ok ......."





