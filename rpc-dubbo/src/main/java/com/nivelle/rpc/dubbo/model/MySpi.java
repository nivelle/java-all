package com.nivelle.rpc.dubbo.model;

import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

/**
 * Dubbo SPI 机制
 *
 * @author nivelle
 * @date 2019/10/07
 */
@SPI(value = "mySpi")
public interface MySpi {
    /**
     * Adaptive 是注解在接口方法上的，表示拓展的加载逻辑需由框架自动生成。
     */
    @Adaptive
    void sayHelloSpi();

}
