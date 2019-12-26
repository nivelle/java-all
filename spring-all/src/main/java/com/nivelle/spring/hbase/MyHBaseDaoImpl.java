package com.nivelle.spring.hbase;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Slf4j
public class MyHBaseDaoImpl {

    private static final String DEFAULT_FAMILY = "f1";

    private static final String SPLIT_CHAR = "-";

    private static final Integer DEFAULT_MAX_NUM = 100;
    
    @Autowired
    private HbaseTemplate hbaseTemplate;

    /**
     * 添加数据
     *
     * @param tableName
     * @param rowKey
     * @param family
     * @param qualifier
     * @param value
     * @return
     */
    public boolean putData(String tableName, String rowKey, String family, String qualifier, String value) {
        boolean flag = false;
        long startTime = System.currentTimeMillis();
        try {
            hbaseTemplate.put(tableName, getRowKey(rowKey), (Strings.isNotBlank(family) ? family : DEFAULT_FAMILY), qualifier, Bytes.toBytes(value));
            return true;
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println("putData cost time:" + (endTime - startTime));
            return flag;
        }
    }

    /**
     * scan 查询数据
     *
     * @param tableName
     * @param rowKey
     * @param family
     * @return
     */
    public List<Map<String, String>> scanData(String tableName, String rowKey, String family) {

        Scan scan = new Scan();
        String startKey = new StringBuffer(rowKey).reverse().toString();
        String stopKey = "|";
        scan.setStartRow(startKey.getBytes());
        scan.setStopRow(stopKey.getBytes());
        scan.addFamily(Strings.isNotBlank(family) ? family.getBytes() : DEFAULT_FAMILY.getBytes());
        scan.setMaxVersions(3);
        try {
            return hbaseTemplate.find(tableName, scan, (ResultScanner resultScanner) -> {
                List<Map<String, String>> list = new ArrayList<>();
                Result[] res = resultScanner.next(DEFAULT_MAX_NUM);
                List<Result> results = Arrays.asList(res);
                results.forEach(result -> {
                    Map<String, String> dataMap = new LinkedHashMap<>();
                    dataMap.put("RowKey", Bytes.toString(result.getRow()));
                    result.listCells().forEach(keyValue -> {
                        //列族
                        String familyStore = Bytes.toString(keyValue.getFamilyArray());
                        //列
                        String qualifierStore = Bytes.toString(keyValue.getQualifierArray());
                        String value = Bytes.toString(keyValue.getValueArray());
                        dataMap.put(getGetKey(familyStore, qualifierStore), value);
                        list.add(dataMap);
                    });
                });
                return list;
            });
        } catch (Exception e) {
            System.out.println("Hbase scan error!" + e);
        }
        return null;
    }

    /**
     * 删除最大版本数据
     *
     * @param tableName
     * @param rowKey
     * @param family
     * @param qualifier
     * @return
     */
    public boolean deleteData(String tableName, String rowKey, String family, String qualifier) {
        try {
            hbaseTemplate.delete(tableName, getRowKey(rowKey), family, qualifier);
            return true;
        } catch (Exception e) {
            System.out.println("HBase delete error!" + e);
        }
        return false;
    }

    /**
     * 删除所有版本的数据
     *
     * @param tableName
     * @param rowKey
     * @param family
     * @param qualifier
     * @return
     */
    public boolean deleteAllData(String tableName, String rowKey, String family, String qualifier) {
        Boolean flag = false;
        try {
            Delete delete = new Delete(getRowKey(rowKey).getBytes());
            delete.addColumns(family.getBytes(), qualifier.getBytes(), System.currentTimeMillis());
            hbaseTemplate.execute(tableName, (HTableInterface hTableInterface) -> {
                hTableInterface.delete(delete);
                return null;
            });
            return true;
        } catch (Exception e) {
            System.out.println("HBase delete error!" + e);
        }
        return flag;

    }


    /**
     * 获取指定数据
     *
     * @param tableName
     * @param rowKey
     * @param family
     * @param qualifier
     * @return
     */
    public Map<String, String> getData(String tableName, String rowKey, String family, String qualifier) {
        Map<String, String> dataMap = Maps.newHashMap();
        try {
            return hbaseTemplate.get(tableName, getRowKey(rowKey), family, qualifier, (Result result, int row) -> {
                dataMap.put(getGetKey(family, qualifier), Bytes.toString(result.getValue(family.getBytes(), qualifier.getBytes())));
                return dataMap;
            });

        } catch (Exception e) {
            System.out.println("HBase getData error!" + e);
        }
        return null;
    }

    /**
     * 列展示 列族:列
     *
     * @param family
     * @param qualifier
     * @return
     */
    private String getGetKey(String family, String qualifier) {
        return family + ":" + qualifier;
    }

    private static String getRowKey(String usr) {
        return new StringBuffer(usr).reverse().append(SPLIT_CHAR).toString();
    }
}
