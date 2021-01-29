package com.nivelle.base.javacore;

/**
 * 死锁问题
 *
 * @author fuxinzhong
 * @date 2021/01/29
 */
public class DeadLockDemo {
    /**
     * Found one Java-level deadlock:
     * =============================
     * "Thread-7":
     * waiting to lock monitor 0x00007f8d0f21fbb8 (object 0x00000007957f77e0, a com.nivelle.base.javacore.Account),
     * which is held by "Thread-1"
     * "Thread-1":
     * waiting to lock monitor 0x00007f8d0f81dd58 (object 0x00000007957f77d0, a com.nivelle.base.javacore.Account),
     * which is held by "Thread-0"
     * "Thread-0":
     * waiting to lock monitor 0x00007f8d0f21fbb8 (object 0x00000007957f77e0, a com.nivelle.base.javacore.Account),
     * which is held by "Thread-1"
     * <p>
     * Java stack information for the threads listed above:
     * ===================================================
     * "Thread-7":
     * at com.nivelle.base.javacore.Account.transfer(DeadLockDemo.java:45)
     * - waiting to lock <0x00000007957f77e0> (a com.nivelle.base.javacore.Account)
     * at com.nivelle.base.javacore.DeadLockDemo.lambda$main$1(DeadLockDemo.java:22)
     * at com.nivelle.base.javacore.DeadLockDemo$$Lambda$2/1637506559.run(Unknown Source)
     * at java.lang.Thread.run(Thread.java:748)
     * "Thread-1":
     * at com.nivelle.base.javacore.Account.transfer(DeadLockDemo.java:47)
     * - waiting to lock <0x00000007957f77d0> (a com.nivelle.base.javacore.Account)
     * - locked <0x00000007957f77e0> (a com.nivelle.base.javacore.Account)
     * at com.nivelle.base.javacore.DeadLockDemo.lambda$main$1(DeadLockDemo.java:22)
     * at com.nivelle.base.javacore.DeadLockDemo$$Lambda$2/1637506559.run(Unknown Source)
     * at java.lang.Thread.run(Thread.java:748)
     * "Thread-0":
     * at com.nivelle.base.javacore.Account.transfer(DeadLockDemo.java:47)
     * - waiting to lock <0x00000007957f77e0> (a com.nivelle.base.javacore.Account)
     * - locked <0x00000007957f77d0> (a com.nivelle.base.javacore.Account)
     * at com.nivelle.base.javacore.DeadLockDemo.lambda$main$0(DeadLockDemo.java:16)
     * at com.nivelle.base.javacore.DeadLockDemo$$Lambda$1/1161082381.run(Unknown Source)
     * at java.lang.Thread.run(Thread.java:748)
     */

    public static void main(String[] args) throws InterruptedException {
        Account accountA = new Account(2);
        Account accountB = new Account(2);
        for (int i = 0; i < 4; i++) {
            Thread threadA = new Thread(() -> {
                boolean result = accountA.transfer(accountB, 2, "a 转账给 b成功");
                if (result) {
                    accountB.transfer(accountA, 2, "b 转账给 a 成功");
                }
            });
            Thread threadB = new Thread(() -> {
                boolean result = accountB.transfer(accountA, 2, "b 转账给 a 成功");
                if (result) {
                    accountA.transfer(accountB, 2, "a 转账给 b成功");
                }
            });
            threadA.start();
            threadB.start();
        }


    }

}

class Account {
    private int balance;

    public Account(int balance) {
        this.balance = balance;
    }

    boolean transfer(Account target, int amt, String desc) {
        synchronized (this) {
            System.out.println("获取锁 this:" + this);
            synchronized (target) {
                System.out.println("获取锁 target:" + target);
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                    System.out.println(desc);
                }
                return true;
            }
        }
    }
}
