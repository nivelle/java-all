package com.nivelle.container.dubbospi;

/**
 * Dubbo spi机制
 *
 * @author nivelle
 * @date 2019/10/07
 */
public class MySpiService2 implements MySpi{

    @Override
    public void sayHelloSpi(){
        System.err.println("spi service 2");
    }

}
