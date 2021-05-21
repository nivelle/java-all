package com.nivelle.core.utils;

import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/05/21
 */
public class ParseData {


    public static void main(String[] args) {
        List<UserData> list = getUsers("/Users/nivellefu/IdeaProjects/java-guides/java-core/src/main/resources/nd_data1A.txt");
        List<String> bookList = getBooks("/Users/nivellefu/IdeaProjects/java-guides/java-core/src/main/resources/book.txt");

        Map<String, List<UserData>> userData = list.stream().collect(Collectors.groupingBy(UserData::getUserName));
        Iterator<String> iterator = userData.keySet().iterator();
        List<HashMap<String, String>> resultList = Lists.newArrayList();
        while (iterator.hasNext()) {
            HashMap<String, String> result = Maps.newHashMap();
            String userNameKey = iterator.next();
            result.put("S号", userNameKey);
            List<UserData> userData1 = userData.get("userNameKey");
            if (!CollectionUtils.isEmpty(userData1)) {
                Map<String, List<UserData>> bookMap = userData1.stream().collect(Collectors.groupingBy(UserData::getBookId));
                result.put("阅读书籍数", String.valueOf(bookMap.size()));
                Iterator<String> iterator1 = bookMap.keySet().iterator();
                int largeFiveCount = 0;
                int intBookListCount = 0;
                while (iterator1.hasNext()) {
                    String bookId = iterator1.next();
                    List<UserData> bookData = bookMap.get(bookId);
                    IntSummaryStatistics ageSummary = bookData.stream().collect(Collectors.summarizingInt(p -> Integer.parseInt(p.getReadTimes())));
                    long readTimes = ageSummary.getSum();
                    if (readTimes >= 5) {
                        largeFiveCount += 1;
                    }
                    if (bookList.contains(bookId)) {
                        intBookListCount += 1;
                    }
                }
                result.put("阅读大于5本的", String.valueOf(largeFiveCount));
                BigDecimal rate = new BigDecimal(String.valueOf(intBookListCount)).divide(new BigDecimal(String.valueOf(bookList.size()))).setScale(4, BigDecimal.ROUND_HALF_UP);
                result.put("重合度", String.valueOf(result));

            } else {
                result.put("阅读书籍数", "0");
            }
            resultList.add(result);
            System.out.println("结果：" + resultList);
        }
    }

    public static List<UserData> getUsers(String filePath) {
        List<UserData> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath)); // 读取CSV文件
            String lineTxt = null;
            while ((lineTxt = reader.readLine()) != null) {
                String[] row = lineTxt.split(","); // 分隔字符串（这里用到转义），存储到List<taskRule>里
                if (row.length == 3) {
                    UserData userData = new UserData(row[0], row[1], row[2]);
                    list.add(userData);
                } else {
                    System.out.println("error=" + lineTxt);
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("read errors :" + e);
        }
        return list;
    }

    public static List<String> getBooks(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath)); // 读取CSV文件

            String lineTxt = reader.readLine();
            if ((lineTxt = reader.readLine()) != null) {
                String[] row = lineTxt.split(",");
                return Arrays.asList(row);
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("read errors :" + e);
        }
        return Lists.newArrayList();
    }
}
