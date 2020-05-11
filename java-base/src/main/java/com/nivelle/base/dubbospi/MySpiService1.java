package com.nivelle.base.dubbospi;

/**
 * Dubbo spi机制
 *
 * @author nivell
 * @date 2019/10/07
 */
public class MySpiService1 implements MySpi{

    @Override
    public void sayHelloSpi(){
        System.err.println("spi service 1");
    }

}
