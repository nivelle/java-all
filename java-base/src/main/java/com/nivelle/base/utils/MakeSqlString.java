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
//        StringBuilder stringBuilder = new StringBuilder("insert into students (name,age,high,gender,cls_id) values");
//        for (int i = 0; i < 20; i++) {
//            stringBuilder.append("(");
//            stringBuilder.append(" \"老").append(i).append("\",");
//            stringBuilder.append(new Random().nextInt(20)).append(",");
//            stringBuilder.append(new Random().nextInt(200)).append(",");
//            if (i / 2 == 0) {
//                stringBuilder.append("1").append(",");
//            } else {
//                stringBuilder.append("2").append(",");
//            }
//            stringBuilder.append("111").append(")").append(",");
//        }
//
//        //System.out.println(stringBuilder.toString());
//
//        //System.out.println(stringBuilder1.toString());
//        int initId = 1141;
//        StringBuilder stringBuilder2 = new StringBuilder();
//        for (int i = 1142; i <= 1255; i++) {
//            stringBuilder2.append(i);
//            stringBuilder2.append(",");
//        }
//        StringBuilder stringBuilder4 = new StringBuilder();
//        //System.out.println(stringBuilder2.toString());
//        for (int i = 64; i < 256; i++) {
//            StringBuilder stringBuilder1 = new StringBuilder("ALTER TABLE `push_info_");
//            stringBuilder1.append(i);
//            stringBuilder1.append("`");
//            stringBuilder1.append(" ADD `company_flag` int(2) NOT NULL DEFAULT '1' COMMENT '是否屏蔽企业 1：否 2：是';");
//            System.out.println(stringBuilder1);
//        }

        for (int i = 0; i < 64; i++) {
            StringBuilder stringBuilder2 = new StringBuilder("DROP TABLE IF EXISTS `/");
            stringBuilder2.append(i);
            stringBuilder2.append("`;");
            stringBuilder2.append("CREATE TABLE `/");
            stringBuilder2.append(i);
            stringBuilder2.append("`");
            stringBuilder2.append("(");
            stringBuilder2.append("`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键唯一自增',");
            stringBuilder2.append("`user_name` varchar(16) NOT NULL default ''COMMENT 'S号',");
            stringBuilder2.append("`uc_user_name` varchar(16) NOT NULL default ''COMMENT '用户中心用户名',");
            stringBuilder2.append("`nd_phone` varchar(16) NOT NULL DEFAULT '' COMMENT '精选手机号码',");
            stringBuilder2.append("`uc_user_phone` varchar(16) NOT NULL DEFAULT '' COMMENT '用户中心手机号码',");
            stringBuilder2.append("`create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',");
            stringBuilder2.append("`update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',");
            stringBuilder2.append("`yn` tinyint(1) NOT NULL DEFAULT '1' COMMENT '数据有效性,0:无效;1:有效',");
            stringBuilder2.append("PRIMARY KEY (`id`),");
            stringBuilder2.append("UNIQUE INDEX `idx_name`(`user_name`)");
            stringBuilder2.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            System.out.println(stringBuilder2.toString());
        }


    }
}
