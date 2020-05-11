package com.nivelle.base.dubbospi;

import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

/**
 * Dubbo SPI 机制
 *
 * @author nivell
 * @date 2019/10/07
 */
@SPI
@Adaptive(value = "MySpiService1")
public interface MySpi {

    void sayHelloSpi();

}
