### 关于ThreadLocal

- JDK1.2的版本中就提供java.lang.ThreadLocal类，每一个ThreadLocal能够放一个线程级别的变量， 它本身能够被多个线程共享使用，并且又能够达到线程安全的目的，且绝对线程安全。

### ThreadLocal包含了四个方法:

- void set(Object value)设置当前线程的线程局部变量的值。
- public Object get()该方法返回当前线程所对应的线程局部变量。
- public void remove()将当前线程局部变量的值删除，其目的是为了减少内存使用，加快内存回收。
- protected Object initialValue()返回该线程局部变量的初始值，该方法是一个protected的方法，目的是为了让子类覆盖而设计的。

-----

### 场景一：代替参数的显式传递

当我们在写API接口的时候，通常Controller层会接受来自前端的入参，当这个接口功能比较复杂的时候，可能我们调用的Service层内部还调用了 很多其他的很多方法，通常情况下，我们会在每个调用的方法上加上需要传递的参数。

但是如果我们将参数存入ThreadLocal中，那么就不用显式的传递参数了，而是只需要ThreadLocal中获取即可。

这个场景其实使用的比较少，一方面显式传参比较容易理解，另一方面我们可以将多个参数封装为对象去传递。

### 场景二：全局存储用户信息

在现在的系统设计中，前后端分离已基本成为常态，分离之后如何获取用户信息就成了一件麻烦事，通常在用户登录后， 用户信息会保存在Session或者Token中。这个时候，我们如果使用常规的手段去获取用户信息会很费劲，拿Session来说，我们要在接口参数中加上HttpServletRequest对象，然后调用 getSession方法，且每一个需要用户信息的接口都要加上这个参数，才能获取Session，这样实现就很麻烦了。

在实际的系统设计中，我们肯定不会采用上面所说的这种方式，而是使用ThreadLocal，我们会选择在拦截器的业务中， 获取到保存的用户信息，然后存入ThreadLocal，那么当前线程在任何地方如果需要拿到用户信息都可以使用ThreadLocal的get()方法 (异步程序中ThreadLocal是不可靠的)

对于笔者而言，这个场景使用的比较多，当用户登录后，会将用户信息存入Token中返回前端，当用户调用需要授权的接口时，需要在header中携带 Token，然后拦截器中解析Token，获取用户信息，调用自定义的类(AuthNHolder)存入ThreadLocal中，当请求结束的时候，将ThreadLocal存储数据清空， 中间的过程无需在关注如何获取用户信息，只需要使用工具类的get方法即可。

````java
public class AuthNHolder {
private static final ThreadLocal<Map<String,String>> loginThreadLocal = new ThreadLocal<Map<String,String>>();

	public static void map(Map<String,String> map){
		loginThreadLocal.set(map);
	}
	public static String userId(){
    		return get("userId");
	}
	public static String get(String key){
    		Map<String,String> map = getMap();
    		return map.get(key);
    }
	public static void clear(){
       loginThreadLocal.remove();
	}

}
````
### 场景三：解决线程安全问题

在Spring的Web项目中，我们通常会将业务分为Controller层，Service层，Dao层， 我们都知道@Autowired注解默认使用单例模式，那么不同请求线程进来之后，由于Dao层使用单例，那么负责数据库连接的Connection也只有一个， 如果每个请求线程都去连接数据库，那么就会造成线程不安全的问题，Spring是如何解决这个问题的呢？

- 在Spring项目中Dao层中装配的Connection肯定是线程安全的，其解决方案就是采用ThreadLocal方法，当每个请求线程使用Connection的时候， 都会从ThreadLocal获取一次，如果为null，说明没有进行过数据库连接，连接后存入ThreadLocal中，如此一来，每一个请求线程都保存有一份 自己的Connection。于是便解决了线程安全问题

- ThreadLocal在设计之初就是为解决并发问题而提供一种方案，每个线程维护一份自己的数据，达到线程隔离的效果。

## 慎用的场景

- 1.线程池中线程调用使用ThreadLocal 由于线程池中对线程管理都是採用线程复用的方法。在线程池中线程非常难结束甚至于永远不会结束。这将意味着线程持续的时间将不可预測，甚至与JVM的生命周期一致

- 2.异步程序中，ThreadLocal的參数传递是不靠谱的， 由于线程将请求发送后。就不再等待远程返回结果继续向下运行了，真正的返回结果得到后，处理的线程可能是其他的线程。Java8中的并发流也要考虑这种情况

- 3.使用完ThreadLocal ，最好手动调用 remove() 方法，防止出现内存溢出，因为中使用的key为ThreadLocal的弱引用， 如果ThreadLocal 没有被外部强引用的情况下，在垃圾回收的时候会被清理掉的，但是如果value是强引用，不会被清理， 这样一来就会出现 key 为 null 的 value。