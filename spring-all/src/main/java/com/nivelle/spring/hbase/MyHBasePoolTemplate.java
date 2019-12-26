
package com.nivelle.spring.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.HbaseUtils;
import org.springframework.data.hadoop.hbase.TableCallback;
import org.springframework.util.Assert;

import java.io.IOException;


public class MyHBasePoolTemplate extends HbaseTemplate {

    private boolean autoFlush = true;

    public MyHBasePoolTemplate(Configuration configuration, int maxNum) {
        super(configuration);
        this.pool = new HTablePool(configuration, maxNum);
    }

    private HTablePool pool = null;

    public HTablePool getPool() {
        return pool;
    }

    public void setPool(HTablePool pool) {
        this.pool = pool;
    }

    @Override
    public <T> T execute(String tableName, TableCallback<T> action) {
        Assert.notNull(action, "Callback object must not be null");
        Assert.notNull(tableName, "No table specified");
        HTableInterface table = this.pool.getTable(tableName);
        try {
            boolean previousFlushSetting = applyFlushSetting(table);
            T result = action.doInTable(table);
            flushIfNecessary(table, previousFlushSetting);
            return result;
        } catch (Throwable th) {
            if (th instanceof Error) {
                throw ((Error) th);
            }
            if (th instanceof RuntimeException) {
                throw ((RuntimeException) th);
            }
            throw convertHbaseAccessException((Exception) th);
        } finally {
            releaseTable(tableName, table);
        }
    }

    @SuppressWarnings("deprecation")
    private boolean applyFlushSetting(HTableInterface table) {
        boolean autoFlush = table.isAutoFlush();
        if (table instanceof HTable) {
            ((HTable) table).setAutoFlush(this.autoFlush);
        }
        return autoFlush;
    }

    private void flushIfNecessary(HTableInterface table, boolean oldFlush) throws IOException {
        table.flushCommits();
        restoreFlushSettings(table, oldFlush);
    }

    @SuppressWarnings("deprecation")
    private void restoreFlushSettings(HTableInterface table, boolean oldFlush) {
        if (table instanceof HTable) {
            if (table.isAutoFlush() != oldFlush) {
                ((HTable) table).setAutoFlush(oldFlush);
            }
        }
    }

    private void releaseTable(String tableName, HTableInterface table) {
        HbaseUtils.releaseTable(tableName, table, getTableFactory());
    }
}
