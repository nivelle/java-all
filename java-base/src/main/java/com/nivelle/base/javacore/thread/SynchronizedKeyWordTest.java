package com.nivelle.base.javacore.thread;

/**
 * @author nivellefu
 */
public class SynchronizedKeyWordTest {


    public synchronized void execute(){
        try{
            System.out.println(Thread.currentThread().getName()+" -> do something synchronize");
            try {
                anotherLock();
                Thread.sleep(5000l);
                Thread.interrupted();
            }catch (InterruptedException e){
                System.out.println(Thread.currentThread().getName()+"interrupted");
                Thread.currentThread().interrupt();
            }

        }catch (Exception e){

        }finally {

        }
    }

    public static void main(String args[]){
        SynchronizedKeyWordTest reentrantLockTest = new SynchronizedKeyWordTest();
        new Thread(()->reentrantLockTest.execute()).start();

        new Thread(()->reentrantLockTest.execute()).start();
    }

    public synchronized void anotherLock() {

        try {
            System.out.println(Thread.currentThread().getName() + " invoke anotherLock");
        } finally {
        }
    }

}
