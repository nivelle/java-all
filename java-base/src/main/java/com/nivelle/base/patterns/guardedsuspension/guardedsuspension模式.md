### 等待唤醒机制（guarded suspension【保护性地暂停】）

[![yeWtQ1.png](https://s3.ax1x.com/2021/02/01/yeWtQ1.png)](https://imgchr.com/i/yeWtQ1)

- get()方法通过条件变量的await()方法实现等待

- onChanged()方法通过条件变量的signalAll()方法实现唤醒功能