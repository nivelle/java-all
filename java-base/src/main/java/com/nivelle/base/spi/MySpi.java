package com.nivelle.base.spi;

import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

/**
 * SPI 机制
 *
 * @author fuxinzhong
 * @date 2019/10/07
 */
@SPI
@Adaptive(value = "MySpiService1")
public interface MySpi {

    void sayHelloSpi();

}
