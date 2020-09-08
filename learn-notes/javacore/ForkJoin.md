
### ForkJoinTask

```
public abstract class ForkJoinTask<V> implements Future<V>, Serializable 

```

### 任务状态

```
//volatie修饰的任务状态值,由ForkJoinPool或工作线程修改.
volatile int status; 
static final int DONE_MASK   = 0xf0000000;//用于屏蔽完成状态位. 
static final int NORMAL      = 0xf0000000;//表示正常完成,是负值.
static final int CANCELLED   = 0xc0000000;//表示被取消,负值,且小于NORMAL
static final int EXCEPTIONAL = 0x80000000;//异常完成,负值,且小于CANCELLED
static final int SIGNAL      = 0x00010000;//用于signal,必须不小于1<<16,默认为1<<16.
static final int SMASK       = 0x0000ffff;//后十六位的task标签

```

### 标记当前task的completion状态,同时根据情况唤醒等待该task的线程.

```
private int setCompletion(int completion) {
    for (int s;;) {
        //开启一个循环,如果当前task的status已经是各种完成(小于0),则直接返回status,这个status可能是某一次循环前被其他线程完成.
        if ((s = status) < 0){
            return s;
        }
        //尝试将原来的status设置为它与completion按位或的结果.
        if (U.compareAndSwapInt(this, STATUS, s, s | completion)) {
            if ((s >>> 16) != 0)
                //此处体现了SIGNAL的标记作用,很明显,只要task完成(包含取消或异常),或completion传入的值不小于1<<16,
                //就可以起到唤醒其他线程的作用.
                synchronized (this) {
                 notifyAll();
                }
            //cas成功,返回参数中的completion.
            return completion;
        }
    }
}

```