package com.nivelle.guide.javacore.thread;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadLocalTest {

    private static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue(){
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public static void main(String args[]){
        new Thread(()->{
            Date date = new Date();
            System.out.println(dateFormatThreadLocal.get().format(date));
        }) .start();

        new Thread(()->{
            Date date = new Date();
            System.out.println(dateFormatThreadLocal.get().format(date));
        }) .start();
    }
}
