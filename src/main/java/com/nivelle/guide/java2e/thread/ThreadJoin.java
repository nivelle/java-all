package com.nivelle.guide.java2e.thread;

public class ThreadJoin {

    public static void main(String args[]){
        Thread thread= new Thread(new Runnable() {
            @Override
            public void run() {
                System.err.println("线程"+Thread.currentThread().getId()+" 打印信息");
            }
        });
        thread.start();

        try {
            thread.join();//调用者不断试探thread是否还或者,如果活着就不断的调用wait
        }catch (Exception e){
            System.err.println(e);
        }

        System.err.println("主线程打印信息");

    }
}
