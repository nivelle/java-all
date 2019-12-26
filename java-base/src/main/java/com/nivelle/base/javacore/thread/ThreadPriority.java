package com.nivelle.base.javacore.thread;

public class ThreadPriority {

    public static void main(String[] args)  {
        new MyThread("低级", 1).start();
        new MyThread("中级", 5).start();
        new MyThread("高级", 10).start();
    }

}

    class  MyThread extends Thread{
        public MyThread(String name,int pro){
            super(name);
            this.setPriority(pro);
        }

        @Override
        public void run(){
            for (int i=0;i<30;i++){
                System.out.println(this.getName()+"线程第"+i+"次执行!");
                if(i%5==0){
                    Thread.yield();
                }
            }
        }
    }

