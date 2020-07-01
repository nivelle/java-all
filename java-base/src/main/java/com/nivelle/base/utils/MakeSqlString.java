package com.nivelle.base.utils;

import java.math.BigDecimal;
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
            if (i/2==0){
                stringBuilder.append("1").append(",");
            }else {
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
            System.out.println(stringBuilder2.toString());

        }
    }
}
