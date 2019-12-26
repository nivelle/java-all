package com.nivelle.base.spi;

/**
 * SPI 机制
 *
 * @author fuxinzhong
 * @date 2019/10/07
 */
public class MySpiService1 implements MySpi{

    @Override
    public void sayHelloSpi(){
        System.err.println("spi service 1");
    }

}
