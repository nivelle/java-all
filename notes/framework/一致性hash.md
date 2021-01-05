
### 原文

[一致性hash,朱双印](http://www.zsythink.net/archives/1182)


### hash散列算法设计原则

- 在一个java应用运行期间，只要一个对象的{@code equals}方法所用到的信息没有被修改,那么对这同一个对象调用多次 hashCode方法，都必须返回同一个整数；在同一个应用程序的多次执行中，每次执行锁返回的整数可以不一样

- 如果两个对象根据{@code equals(Object)}方法进行比较结果是相等的,那么调用这两个对象的{@code hashCode}方法必须返回同样的结果

- 如果两个对象根据{@link java.lang.Object#equals(java.lang.Object)}进行比较是不相等的,那么并<em>不</em>要求调用这两个对象的{@code hashCode}方法必须返回不同的整数结果.给不相等的对象产生不同的整数结果,可能提高散列表(hash tables)的性能


#### java常见散列算法等价：

````
hash =  s[0]*M^(i-1) + s[1]*M^(i-2) + ... + s[i-1];

n = 2^x;

求在hash % n余数最多的情况下M的取值.

````
求在hash % n余数最多的情况下M的取值

而如果M为素数,上面的hash的计算表达式里相当于每项都有了素数,那么hash % n时也就近似相当于素数对n取模,这个时候余数也就会尽可能的多.

之所以选择31,除了Effective Java一书中提到的计算机计算31比较快(可以直接采用位移操作得到 1<<5-1)之外,个人认为还有一个原因:Java是美帝人编写的语言,再加上大多数情况下我们都是采用String作为key,曾有人对超过5W个英文单词做了测试,在常量取31情况下,碰撞的次数都不超过7次(来自Stackoverflow,本人没去验证).

#### boolean hashcode 固定的原因

- Boolean只有true和false两个值,理论上任何两个素数都可以.但是在实际使用时,可能作为key的不只是Boolean一种类型啊,可能还会有其它类型,比如最常见的字符串作为key,还有Integer作为key.至少要保证避开常见hashCode的取值范围吧,Integer还缓存了常用的256个数字着呢…但是太大了也没意义,比如说字符串"00"的hashCode为1536,Boolean的hashCode取值太大的话,指不定又跟字符串的hashCode撞上了,更别说其它对象的了.

- 所以Boolean的hashCode取值也是一个不能太小也不能太大的事情,至于取值1231和1237就真的没有什么数学上的依据了,更大程度上就是Boolean作者个人爱好罢了.