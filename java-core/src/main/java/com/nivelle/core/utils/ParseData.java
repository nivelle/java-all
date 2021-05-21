package com.nivelle.core.utils;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.FileReader;
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
        List<UserData> list = getUsers("/Users/nivellefu/IdeaProjects/java-guides/java-core/src/main/resources/nd_data1B.txt");
        List<String> bookList = getBooks();
        System.out.println("list size:" + list.size());
        System.out.println("bookList size:" + bookList.size());

        Map<String, List<UserData>> userData = list.stream().collect(Collectors.groupingBy(UserData::getUserName));
        Iterator<String> iterator = userData.keySet().iterator();
        List<HashMap<String, Object>> resultList = Lists.newArrayList();
        while (iterator.hasNext()) {
            HashMap<String, Object> result = Maps.newHashMap();
            String userNameKey = iterator.next();
            result.put("s号", userNameKey);
            List<UserData> userData1 = userData.get(userNameKey);
            if (!CollectionUtils.isEmpty(userData1)) {
                Map<String, List<UserData>> bookMap = userData1.stream().collect(Collectors.groupingBy(UserData::getBookId));
                result.put("阅读书籍数", bookMap.size());
                Iterator<String> iterator1 = bookMap.keySet().iterator();
                int largeFiveCount = 0;
                int largeFiveCount1 = 0;
                int intBookListCount = 0;
                while (iterator1.hasNext()) {
                    String bookId = iterator1.next();
                    List<UserData> bookData = bookMap.get(bookId);
                    IntSummaryStatistics ageSummary = bookData.stream().collect(Collectors.summarizingInt(p -> Integer.parseInt(p.getReadTimes())));
                    long readTimes = ageSummary.getSum();
                    if (readTimes >= 5) {
                        largeFiveCount += 1;
                    }
                    if (readTimes >= 5 && bookList.contains(bookId)) {
                        largeFiveCount1 += 1;
                    }
                    if (bookList.contains(bookId)) {
                        intBookListCount += 1;
                    }
                }
                result.put("大于5本数", largeFiveCount);
                if (bookMap.size() > 0) {
                    BigDecimal rate = new BigDecimal(String.valueOf(intBookListCount)).divide(new BigDecimal(String.valueOf(bookMap.size())),4, BigDecimal.ROUND_HALF_UP);
                    result.put("阅读书籍数的重合率", rate);
                } else {
                    result.put("阅读书籍数的重合率", 0);

                }

                if (largeFiveCount > 0) {
                    BigDecimal rate2 = new BigDecimal(String.valueOf(largeFiveCount1)).divide(new BigDecimal(String.valueOf(largeFiveCount)),4, BigDecimal.ROUND_HALF_UP);
                    result.put("大于5分钟的书籍重合率", rate2);

                } else {
                    result.put("大于5分钟的书籍重合率", 0);

                }

            } else {
                result.put("阅读书籍数", "0");
            }
            resultList.add(result);
        }
        System.out.println("结果：" + GsonUtils.toJson(resultList));


        ExcelWriter writer = ExcelUtil.getWriter("/Users/nivellefu/IdeaProjects/java-guides/java-core/src/main/resources/nd_data1BResult.xlsx");
        writer.merge(5, "2期A测试");
        writer.write(resultList, true);
        writer.close();


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

    public static List<String> getBooks() {
        String books = "11854905,11338081,11126631,12510425,12511967,11637993,11808729,11228986,11687785,11790621,12134046,11797834,11341779,12489746,11867517,11280565,12509839,12342721,12440586,12130637,11808729,11500988,10915090,11126634,11126631,11330940,12475909,12493474,12475918,12475910,12475911,12475916,12458800,12468907,11828355,11692186,12457907,11547162,12475786,12512013,12458596,12515312,12129726,12469577,12510322,12510323,10124577,10972952,10972989,12469578,11009941,12469570,12515309,12458596,12492795,12454724,11228986,12510371,12511967,12191188,11797889,11692159,12350471,11622574,12289452,12465987,12516668,12506366,11633022,11633023,11633024,11769821,11769820,11659316,12515315,12506366,12484015,12493430,11739261,11903729,12519132,12332975,12515309,12469570,12510318,12510319,12510321,12469572,12458596,12510317,12510320,11002692,12469579,12515312,12469569,12469568,12469576,12469577,12510323,12510322,12458599,12458598,12469578,11138811,11225058,11789762,12469575,12469573,12342723,12413783,11765323,12151715,12438412,12413552,12456891,12465989,10869613,11481592,11787692,12413641,11389050,10895047,12444338,12516371,12302609,12519141,12519140,12519132,12520715,12520714,12520713,12521432,12458793,11764503,11764504,12520720,12458799,12458800,12475915,12458798,12520707,12520712,11837887,12457907,12474815,12134047,11527970,12179561,11810840,12042183,11513592,11746392,11712761,11775569,11720239,10880623,11192907,12519110,12430539,12510322,11633024,11128080,11225087,11721117,12302003,12468854,12356759,10084874,11673589,12135028,12255527,11670323,11885882,12125881,11662761,11787153,12455168,11126869,12511967,10894984,12438222,11138794,11074707,11670338,12263650,11815975,12412425,11814438,11814439,11108479,11138811,12516388,11126631,12510425,12519243,11742970,11780873,11192073,11809771,11660731";
        String[] bookArray = books.split(",");
        List<String> result = Arrays.asList(bookArray);
        return result;
    }
}
