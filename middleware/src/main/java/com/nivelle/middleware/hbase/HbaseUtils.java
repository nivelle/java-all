

package com.nivelle.middleware.hbase;

import com.google.common.base.Strings;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


public class HbaseUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HbaseUtils.class);

    /**
     * 根据数据类型添加列值
     *
     * @param family 列簇
     * @param col    列名
     * @param value  列值
     * @param type   列值数据类型
     */
    public static void add(Put put, String family, String col, String value, String type) {
        byte[] vals;
        try {
            if (type.equals("int")) {
                int val = Integer.valueOf(value);
                vals = Bytes.toBytes(val);
            } else if (type.equals("boolean")) {
                boolean val = Boolean.parseBoolean(value);
                vals = Bytes.toBytes(val);
            } else if (type.equals("long")) {
                long val = Long.valueOf(value);
                vals = Bytes.toBytes(val);
            } else if (type.equals("float")) {
                float val = Float.valueOf(value);
                vals = Bytes.toBytes(val);
            } else if (type.equals("double")) {
                double val = Double.valueOf(value);
                vals = Bytes.toBytes(val);
            } else if (type.equals("short")) {
                short val = Short.valueOf(value);
                vals = Bytes.toBytes(val);
            } else {
                vals = Bytes.toBytes(value);
            }
        } catch (Exception e) {

            vals = Bytes.toBytes(value);
        }
        put.add(Bytes.toBytes(family), Bytes.toBytes(col), vals);
    }

    /**
     * 根据type将字节数组bytes转化为不同类型的数据
     *
     * @param bytes 原字节数组
     * @param type  数据类型
     * @return 转换后的数据转为String
     */
    public static String getByType(byte[] bytes, String type) {
        String value = Bytes.toString(bytes);
        try {
            if (type.equals("int")) {
                int val = Bytes.toInt(bytes);
                value = Integer.toString(val);
            } else if (type.equals("boolean")) {
                boolean val = Bytes.toBoolean(bytes);
                value = Boolean.toString(val);
            } else if (type.equals("long")) {
                long val = Bytes.toLong(bytes);
                value = Long.toString(val);
            } else if (type.equals("float")) {
                float val = Bytes.toFloat(bytes);
                value = Float.toString(val);
            } else if (type.equals("double")) {
                double val = Bytes.toDouble(bytes);
                value = Double.toString(val);
            } else if (type.equals("short")) {
                short val = Bytes.toShort(bytes);
                value = Short.toString(val);
            }
        } catch (Exception e) {
            LOG.error("HbasePutUtils getByType exception : bytes = {}, type = {}", Bytes.toString(bytes), type, e);
            value = Bytes.toString(bytes);
        }
        return value;
    }

    /**
     * 得到Scan
     *
     * @param startRow 起始行
     * @param endRow   结束行
     * @param families 列簇组
     * @param cols     列组
     * @return
     */
    public static Scan getScan(String startRow, String endRow, String families, String cols) {
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes(startRow));
        scan.setStopRow(Bytes.toBytes(endRow));
        if (Strings.isNullOrEmpty(families)) {
            return scan;
        }
        Arrays.asList(families.split(",")).forEach(family -> {
            scan.addFamily(family.getBytes());
            if (!Strings.isNullOrEmpty(cols)) {
                Arrays.asList(cols.split(",")).forEach(col -> {
                    scan.addColumn(family.getBytes(), col.getBytes());
                });
            }
        });
        return scan;
    }

    /**
     * 得到Delete
     *
     * @param rowKey 行键
     * @param family 列簇
     * @param cols   列名组
     * @return
     */
    public static Delete getDelete(String rowKey, String family, String cols) {
        Delete delete = new Delete(Bytes.toBytes(rowKey));

        if (Strings.isNullOrEmpty(family)) {
            return delete;
        }
        if (Strings.isNullOrEmpty(cols)) {
            // 列簇不为空列名为空时，删除rowKey中列簇对应的列
            delete.deleteFamily(Bytes.toBytes(family));
        } else {
            // 列簇不为空且列名不为空时，删除rowKey中列簇特定的columns列
            Arrays.asList(cols.split(",")).forEach(col -> delete.deleteColumns(Bytes.toBytes(family), Bytes.toBytes(col)));
        }
        return delete;
    }

}
