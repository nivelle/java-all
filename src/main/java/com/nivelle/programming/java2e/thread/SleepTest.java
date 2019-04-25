package com.nivelle.programming.java2e.thread;

public class SleepTest {
  public static void main(String[] args) {
      MySleepRunnable myRunnable = new MySleepRunnable();
             Thread thread = new Thread(myRunnable);
             for (int i = 0; i < 100; i++) {
             System.out.println(Thread.currentThread().getName() + " " + i);
             if (i == 30) {
                thread.start();
             try {
                     Thread.sleep(1);   // 使得thread必然能够马上得以执行14                
                      } catch (InterruptedException e) {
                     e.printStackTrace();
               }
            }
        }
    }
}
class MySleepRunnable implements Runnable {
      @Override
      public void run() {
     for (int i = 0; i < 100; i++) {
             System.out.println(Thread.currentThread().getName() + " " + i);
        }
   }
 }