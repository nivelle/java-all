package com.nivelle.spring.hbase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * HBASE使用实例
 *
 * @author fuxinzhong
 * @date 2019/11/05
 */
@Controller
@RequestMapping("test/hbase")
@Slf4j
public class HBaseController {

    @Autowired
    MyHBaseDaoImpl myHBaseDaoImpl;

    @RequestMapping("/put/{tableName}/{rowKey}/{family}/{qualifier}/{value}")
    @ResponseBody
    public Object put(@PathVariable String tableName, @PathVariable String rowKey, @PathVariable String family, @PathVariable String qualifier, @PathVariable String value) {
        myHBaseDaoImpl.putData(tableName, rowKey, family, qualifier, value);
        return true;
    }

    @RequestMapping("/scan/{tableName}/{rowKey}/{family}")
    @ResponseBody
    public Object scan(@PathVariable String tableName, @PathVariable String rowKey, @PathVariable String family) {
        return myHBaseDaoImpl.scanData(tableName, rowKey, family);
    }

    @RequestMapping("/delete/{tableName}/{rowKey}/{family}/{qualifier}")
    @ResponseBody
    public Object delete(@PathVariable String tableName, @PathVariable String rowKey, @PathVariable String family, @PathVariable String qualifier) {
        return myHBaseDaoImpl.deleteData(tableName, rowKey, family, qualifier);
    }

    @RequestMapping("/get/{tableName}/{rowKey}/{family}/{qualifier}")
    @ResponseBody
    public Object get(@PathVariable String tableName, @PathVariable String rowKey, @PathVariable String family, @PathVariable String qualifier) {
        return myHBaseDaoImpl.getData(tableName, rowKey, family, qualifier);
    }

    @RequestMapping("/deleteAll/{tableName}/{rowKey}/{family}/{qualifier}")
    @ResponseBody
    public Object deleteAll(@PathVariable String tableName, @PathVariable String rowKey, @PathVariable String family, @PathVariable String qualifier) {
        return myHBaseDaoImpl.deleteAllData(tableName, rowKey, family, qualifier);
    }


}
