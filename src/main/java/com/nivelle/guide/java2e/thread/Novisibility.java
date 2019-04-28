package com.nivelle.guide.java2e.thread;

public class Novisibility{
	private static boolean ready;
	private static int number;

	private static class ReaderThread extends Thread{

	  public void run(){
	  	while(!ready){
	  		Thread.yield();
	  		System.out.println(number);
	  	}
	  }	
	  public static void main(String[] args) {
	  	new ReaderThread().start();
	  	number=42;
	  	ready=true;
	  }
	}
}