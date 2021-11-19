package com.nivelle.core.utils;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import org.assertj.core.util.Lists;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/05/21
 */
public class ParseJsonToExcel {


    public static void main(String[] args) {
//        String path = "/Users/nivellefu/IdeaProjects/java-guides/java-core/src/main/resources/rank.txt";
//        List<UserRankData> list = getData(path);
//        System.out.println(list.size());
//        System.out.println(list.get(0));
//
//        List<HashMap<String, Object>> resultList = Lists.newArrayList();
//
//        for (int i = 0; i < list.size(); i++) {
//            HashMap<String, Object> result = Maps.newHashMap();
//            UserRankData userRankData = list.get(i);
//            System.out.println(userRankData);
//            result.put("排名", userRankData.getRank());
//            result.put("s号", userRankData.getUserName());
//            result.put("企业帐号", userRankData.getCompanyUser());
//            result.put("企业昵称", userRankData.getNick());
//            result.put("手机", userRankData.getPhone());
//            result.put("时长", userRankData.getReadTimes());
//            result.put("分组", userRankData.getGroupId());
//            resultList.add(result);
//        }
//        ExcelWriter writer = ExcelUtil.getWriter("/Users/nivellefu/IdeaProjects/java-guides/java-core/src/main/resources/guangda.xlsx");
//        writer.merge(8, "光大银行前5千排名");
//        writer.write(resultList, true);
//        writer.close();


        String path = "/Users/nivellefu/IdeaProjects/java-guides/java-core/src/main/resources/rank.txt";
        List<InvitedCodeData> list = getData(path);
        System.out.println(list.size());
        System.out.println(list.get(0));

        List<HashMap<String, Object>> resultList = Lists.newArrayList();

        for (int i = 0; i < list.size(); i++) {
            HashMap<String, Object> result = Maps.newHashMap();
            InvitedCodeData invitedCodeData = list.get(i);
            System.out.println(invitedCodeData);
            result.put("邀请码", invitedCodeData.getBookNewsInviteCode());
            result.put("被邀请S号", invitedCodeData.getInvitedUserName());
            result.put("渠道", invitedCodeData.getChannel());
            result.put("版本", invitedCodeData.getVersion());
            result.put("时间", invitedCodeData.getCreateTime());

            resultList.add(result);
        }
        ExcelWriter writer = ExcelUtil.getWriter("/Users/nivellefu/IdeaProjects/java-guides/java-core/src/main/resources/booknews.xlsx");
        writer.merge(8, "书讯邀请码2021-11-17 :23:00至2021-11-19 19:11:00");
        writer.write(resultList, true);
        writer.close();
    }


    public static List<InvitedCodeData> getData(String filePath) {
        String jsonStr = "";
        try {
            File jsonFile = new File(filePath);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<InvitedCodeData> list = GsonUtils.fromJson(jsonStr, new TypeToken<List<InvitedCodeData>>() {
                }.getType());
        return list;
    }

}
