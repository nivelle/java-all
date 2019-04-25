package com.nivelle.programming.java2e.thread;

public class YieldTest{
	public static void main(String[] args) {
		Thread myThread1=new MyThread1();
		Thread myThread2=new MyThread2();
		myThread1.setPriority(Thread.MAX_PRIORITY);
		myThread2.setPriority(Thread.MIN_PRIORITY);
		for (int i=0;i<100 ;i++ ) {
			System.out.println("main thread i = " + i);
			if(i==20){
				myThread1.start();
				myThread2.start();
				Thread.yield();
			}
		}
	}
}
class MyThread1 extends Thread{
	public void run(){
		for (int i=0;i<100 ;i++ ) {
			  System.out.println("myThread 1 --  i = " + i);
		}
	}
}
class MyThread2 extends Thread{
	public void run(){
		for (int i=0;i<100 ;i++ ) {
			 System.out.println("myThread 2 --  i = " + i);
		}
	}
}