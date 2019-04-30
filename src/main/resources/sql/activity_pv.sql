DROP TABLE IF EXISTS `activity_pv`;
CREATE TABLE `activity_pv` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` varchar(32) NOT NULL default '' COMMENT '活动id',
  `position_type` tinyint(10) NOT NULL default 0 COMMENT '广告位置 1.活动页 2.首页',
  `ip` varchar (15) NOT NULL DEFAULT '' COMMENT 'ip地址',
  `device_type` varchar(32) NOT NULL default '' COMMENT '设备类型',
  `device_no` varchar (32) NOT NULL default '' COMMENT '设备号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


GRANT ALL PRIVILEGES ON javaguides.* TO 'root'@'localhost' IDENTIFIED BY 'Nivelle123' WITH GRANT OPTION;


create user 'root'@'%' identified with mysql_native_password by 'Nivelle123';


