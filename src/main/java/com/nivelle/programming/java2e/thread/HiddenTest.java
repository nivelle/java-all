package com.nivelle.programming.java2e.thread;

public class HiddenTest{
	public static void main(String[] args) {
		Thread myThread=new MyThreadHidden();
		for (int i=0;i<100 ;i++ ) {
			  System.out.println("main thread i = " + i);
			  if(i==20){
			  	myThread.setDaemon(true);
			  	myThread.start();
			  }
		}
	}
}
class MyThreadHidden extends Thread{
	public void run(){
		for (int i=0;i<100 ;i++ ) {
			System.out.println("i = " + i);
			try{
				Thread.sleep(1);
			}catch(InterruptedException e){
               e.printStackTrace();
			}
		}
	}
}