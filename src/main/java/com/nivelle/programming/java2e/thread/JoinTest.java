package com.nivelle.programming.java2e.thread;

public class JoinTest{
	public static void main(String[] args) {
		MyRunnable myRunnable=new MyRunnable();
		Thread thread=new Thread(myRunnable);
		for (int i=0; i<100;i++ ) {
			 System.out.println(Thread.currentThread().getName() + " " + i);
			 if(i==30){
			 	thread.start();
			 	try{
			 		//主线程调用其他线程的join，等它完成。
			 		thread.join();
			 	}catch(InterruptedException e){
                     e.printStackTrace();
			 	}
			 }
		}
	}
}

class MyJoinRunnable implements Runnable{
	@Override
	public void run(){
		for (int i=0;i<100 ;i++ ) {
		    System.out.println(Thread.currentThread().getName() + " " + i);	
		}
	}
}