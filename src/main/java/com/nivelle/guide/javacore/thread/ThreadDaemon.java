package com.nivelle.guide.javacore.thread;

/**
 * daemon
 */
public class ThreadDaemon{
	public static void main(String[] args) {
		Thread myThread=new MyThreadHidden();
		for (int i=0;i<10 ;i++ ) {
			  System.out.println("main thread i = " + i);
			  if(i==2){
			  	myThread.setDaemon(true);
			  	myThread.start();
			  }
		}
	}
}
class MyThreadHidden extends Thread{
	public void run(){
		for (int i=0;i<10 ;i++ ) {
			System.out.println("i = " + i);
			try{
				Thread.sleep(1);
			}catch(InterruptedException e){
               e.printStackTrace();
			}
		}
	}
}