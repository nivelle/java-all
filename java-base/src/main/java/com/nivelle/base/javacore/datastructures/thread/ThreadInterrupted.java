package com.nivelle.base.javacore.datastructures.thread;

/**
 * @author nivellefu
 */
public class ThreadInterrupted {


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
            System.err.println(e);
        }finally {

        }
    }

    public static void main(String args[]){
        ThreadInterrupted reentrantLockTest = new ThreadInterrupted();
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
