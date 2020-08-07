package com.nivelle.base.utils;

import java.util.Random;

/**
 * 生成sqL
 *
 * @author fuxinzhong
 * @date 2020/06/17
 */
public class MakeSqlString {


    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder("insert into students (name,age,high,gender,cls_id) values");
        for (int i = 0; i < 20; i++) {
            stringBuilder.append("(");
            stringBuilder.append(" \"老").append(i).append("\",");
            stringBuilder.append(new Random().nextInt(20)).append(",");
            stringBuilder.append(new Random().nextInt(200)).append(",");
            if (i / 2 == 0) {
                stringBuilder.append("1").append(",");
            } else {
                stringBuilder.append("2").append(",");
            }
            stringBuilder.append("111").append(")").append(",");
        }

        //System.out.println(stringBuilder.toString());


        for (int i = 0; i < 64; i++) {
            StringBuilder stringBuilder2 = new StringBuilder("alter table `nd_rt_company_user_");
            stringBuilder2.append(i);
            stringBuilder2.append("`");
            stringBuilder2.append("add column `channel` varchar(16) NOT NULL default  '' COMMENT '渠道';");
            // System.out.println(stringBuilder2.toString());
        }


        for (int i = 0; i < 64; i++) {
            StringBuilder stringBuilder2 = new StringBuilder("DROP TABLE IF EXISTS `nd_read_data_record_");
            stringBuilder2.append(i);
            stringBuilder2.append("'");
            stringBuilder2.append("CREATE TABLE `nd_user_read_data_");
            stringBuilder2.append(i);
            stringBuilder2.append("`");
            stringBuilder2.append("(");
            stringBuilder2.append("`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键自增id',");
            stringBuilder2.append("`user_name` varchar(16) NOT NULL default ''COMMENT 'S号',");
            stringBuilder2.append("`book_id` varchar(16) NOT NULL default  '' COMMENT '书籍id',");
            stringBuilder2.append("`read_times` bigint(32) NOT NULL COMMENT '阅读时长（单位分）',");
            stringBuilder2.append("`process` int (20) NOT NULL DEFAULT 0 COMMENT '进度',");
            stringBuilder2.append("`date` varchar(16) NOT NULL default ''COMMENT '日期',");
            stringBuilder2.append("`channel` varchar(16) NOT NULL default  '' COMMENT '渠道',");
            stringBuilder2.append("`create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',");
            stringBuilder2.append("`update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',");
            stringBuilder2.append("PRIMARY KEY (`id`),");
            stringBuilder2.append(" UNIQUE KEY `uniq_user_book_channel` (`user_name`,`book_id`,`channel`)");
            stringBuilder2.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            // System.out.println(stringBuilder2.toString());
        }

        for (int i = 0; i < 64; i++) {
            StringBuilder stringBuilder2 = new StringBuilder("DROP TABLE IF EXISTS `nd_user_read_data_");
            stringBuilder2.append(i);
            stringBuilder2.append("`;");
            stringBuilder2.append("CREATE TABLE `nd_user_read_data_");
            stringBuilder2.append(i);
            stringBuilder2.append("`");
            stringBuilder2.append("(");
            stringBuilder2.append("`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键自增id',");
            stringBuilder2.append("`user_name` varchar(16) NOT NULL default ''COMMENT 'S号',");
            stringBuilder2.append("`book_id` varchar(16) NOT NULL default  '' COMMENT '书籍id',");
            stringBuilder2.append("`read_times` bigint(32) NOT NULL COMMENT '阅读时长（单位分）',");
            stringBuilder2.append("`process` int (20) NOT NULL DEFAULT 0 COMMENT '进度',");
            stringBuilder2.append("`date` varchar(16) NOT NULL default ''COMMENT '日期',");
            stringBuilder2.append("`channel` varchar(16) NOT NULL default  '' COMMENT '渠道',");
            stringBuilder2.append("`create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',");
            stringBuilder2.append("`update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',");
            stringBuilder2.append("PRIMARY KEY (`id`),");
            stringBuilder2.append(" UNIQUE KEY `uniq_user_book_data` (`user_name`,`book_id`,`date`)");
            stringBuilder2.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            //System.out.println(stringBuilder2.toString());
        }

        for (int i = 0; i < 64; i++) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("delete from `nd_user_read_data_");
            stringBuilder2.append(i);
            stringBuilder2.append("`");
            stringBuilder2.append(";");
            //System.out.println(stringBuilder2.toString());
        }

        for (int i = 0; i < 64; i++) {
            StringBuilder stringBuilder2 = new StringBuilder("DROP TABLE IF EXISTS `nd_company_read_time_rank_");
            stringBuilder2.append(i);
            stringBuilder2.append("`;");
            stringBuilder2.append("CREATE TABLE `nd_company_read_time_rank_");
            stringBuilder2.append(i);
            stringBuilder2.append("`");
            stringBuilder2.append("(");
            stringBuilder2.append("`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键自增id',");
            stringBuilder2.append("`user_name` varchar(16) NOT NULL default ''COMMENT 'S号',");
            stringBuilder2.append("`company_id` varchar(16) NOT NULL default ''COMMENT '企业ID',");
            stringBuilder2.append("`read_times` bigint(32) NOT NULL COMMENT '阅读时长（单位分）',");
            stringBuilder2.append("`create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',");
            stringBuilder2.append("`update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',");
            stringBuilder2.append("PRIMARY KEY (`id`),");
            stringBuilder2.append("  UNIQUE KEY `uniq_user_company` (`user_name`,`company_id`)");
            stringBuilder2.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            // System.out.println(stringBuilder2.toString());
        }
        StringBuilder stringBuilder1 = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            StringBuilder stringBuilder2 = new StringBuilder("select * from `nd_user_read_data_");
            stringBuilder2.append(i);
            stringBuilder2.append("`");
            stringBuilder2.append("where process =0 and read_times> 60 and create_time >='2020-08-01 00:00:00' and create_time <='2020-08-07 23:59:59'");
            stringBuilder2.append(" ");
            stringBuilder2.append("union all");
            stringBuilder1.append(" ");
            stringBuilder1.append(stringBuilder2);
        }
        System.out.println(stringBuilder1.toString());
    }
}
