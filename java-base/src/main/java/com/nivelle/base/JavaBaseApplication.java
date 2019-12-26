package com.nivelle.base;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.nivelle.base.spi.MySpi;
import com.sun.tools.javac.util.ServiceLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Iterator;

@SpringBootApplication
@ComponentScan(basePackages = "com.nivelle.base")
public class JavaBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaBaseApplication.class, args);

        //JDK SPI机制
        ServiceLoader<MySpi> serviceLoader = ServiceLoader.load(MySpi.class);
        System.out.println("Java SPI");
        Iterator iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            System.err.println(iterator.next());
        }
        serviceLoader.forEach(MySpi::sayHelloSpi);

        /**
         * dubbo的spi机制
         */
        ExtensionLoader<MySpi> extensionLoader = ExtensionLoader.getExtensionLoader(MySpi.class);

        MySpi mySpi1 = extensionLoader.getExtension("MySpiService1");
        mySpi1.sayHelloSpi();
        MySpi mySpi2 = extensionLoader.getExtension("MySpiService2");
        mySpi2.sayHelloSpi();
    }

}
