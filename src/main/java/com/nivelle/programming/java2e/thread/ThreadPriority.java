package com.nivelle.programming.java2e.thread;

public class ThreadPriority{
	public static void main(String[] args) {
		MyPriorityThread myThread=new MyPriorityThread ();
		for (int i=0;i<100 ;i++ ) {
			System.out.println("main thread i="+i);
			if(i==20){
				myThread.setPriority(Thread.MAX_PRIORITY);
				myThread.start();
			}
		}
	}
}
class MyPriorityThread extends Thread{
	public void run(){
		for (int i=0;i<100 ;i++ ) {
			 System.out.println("i = " + i);
		}
	}
}