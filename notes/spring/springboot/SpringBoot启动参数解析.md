### javaagent

#### 使用

##### Javaagent是java命令的一个参数。参数 javaagent 可以用于指定一个 jar 包，并且对该 java 包有2个要求

- 这个 jar 包的 MANIFEST.MF 文件必须指定 Premain-Class 项。

- Premain-Class 指定的那个类必须实现 premain() 方法。


````
-javaagent:<jarpath>[=<options>]

````

#### 作用
````
代理 (agent) 是在你的main方法前的一个拦截器 (interceptor)，也就是在main方法执行之前，执行agent的代码

agent的代码与你的main方法在同一个JVM中运行,并被同一个system classloader装载,被同一的安全策略 (security policy) 和上下文 (context) 所管理。

````

#### 实现

JVM 会优先加载 带 Instrumentation 签名的方法，加载成功忽略第二种，如果第一种没有，则加载第二种方法。这个逻辑在sun.instrument.InstrumentationImpl
````
public static void premain(String agentArgs, Instrumentation inst)
    
public static void premain(String agentArgs)

````

1. Agent 类必须打成jar包，然后里面的 META-INF/MAINIFEST.MF 必须包含 Premain-Class这个属性

````
Manifest-Version: 1.0
Premain-Class:MyAgent1
Created-By:1.6.0_06

````

2. 所有的这些Agent的jar包,都会自动加入到程序的classpath中.所以不需要手动把他们添加到classpath。除非你想指定classpath的顺

3. 一个java程序中-javaagent这个参数的个数是没有限制的，所以可以添加任意多个java agent。所有的java agent会按照你定义的顺序执行;放在main函数之后的premain是不会被执行的

````
 java -javaagent:MyAgent1.jar -javaagent:MyAgent2.jar -jar MyProgram.jar

````

4. 每一个java agent 都可以接收一个字符串类型的参数，也就是premain中的agentArgs，这个agentArgs是通过java option中定义的.

````
java -javaagent:MyAgent2.jar=thisIsAgentArgs -jar MyProgram.jar
````

5. 通过java agent就可以不用修改原有的java程序代码，通过agent的形式来修改或者增强程序了，或者做热启动等等

### JVM启动后动态 Instrument

上面介绍的Instrumentation是在 JDK 1.5中提供的，开发者只能在main加载之前添加手脚，在 Java SE 6 的 Instrumentation 当中，提供了一个新的代理操作方法：agentmain，可以在 main 函数开始运行之后再运行。

````
//采用attach机制，被代理的目标程序VM有可能很早之前已经启动，当然其所有类已经被加载完成，这个时候需要借助Instrumentation#retransformClasses(Class<?>... classes)让对应的类可以重新转换，从而激活重新转换的类执行ClassFileTransformer列表中的回调
public static void agentmain (String agentArgs, Instrumentation inst)

public static void agentmain (String agentArgs)
````
1. 在Java6 以后实现启动后加载的新实现是Attach api。Attach API 很简单，只有 2 个主要的类，都在 com.sun.tools.attach 包里面：

VirtualMachine 字面意义表示一个Java 虚拟机,也就是程序需要监控的目标虚拟机,提供了获取系统信息(比如获取内存dump、线程dump,类信息统计(比如已加载的类以及实例个数等),
loadAgent，Attach 和 Detach （Attach 动作的相反行为，从 JVM 上面解除一个代理）等方法，可以实现的功能可以说非常之强大 。该类允许我们通过给attach方法传入一个jvm的pid(进程id)，远程连接到jvm上 。

代理类注入操作只是它众多功能中的一个，通过 loadAgent 方法向jvm注册一个代理程序agent,在该agent的代理程序中会得到一个Instrumentation实例,该实例可以 在class加载前改变class的字节码，也可以在class加载后重新加载。在调用Instrumentation实例的方法时，这些方法会使用ClassFileTransformer接口中提供的方法进行处理。

2. VirtualMachineDescriptor 则是一个描述虚拟机的容器类，配合 VirtualMachine 类完成各种功能。

[![DDGYVJ.jpg](https://s3.ax1x.com/2020/11/27/DDGYVJ.jpg)](https://imgchr.com/i/DDGYVJ)


instrument的底层实现依赖于JVMTI(JVM Tool Interface)，它是JVM暴露出来的一些供用户扩展的接口集合，JVMTI是基于事件驱动的，JVM每执行到一定的逻辑就会调用一些事件的回调接口（如果有的话），这些接口可以供开发者去扩展自己的逻辑。JVMTIAgent是一个利用JVMTI暴露出来的接口提供了代理启动时加载(agent on load)、代理通过attach形式加载(agent on attach)和代理卸载(agent on unload)功能的动态库。而instrument agent可以理解为一类JVMTIAgent动态库，别名是JPLISAgent(Java Programming Language Instrumentation Services Agent)，也就是专门为java语言编写的插桩服务提供支持的代理。

#### 启动时加载instrument agent过程：

1. 创建并初始化 JPLISAgent；

2. 监听 VMInit 事件，在 JVM 初始化完成之后做下面的事情：

   - 创建 InstrumentationImpl 对象 ；

   - 监听 ClassFileLoadHook 事件 ；

   - 调用 InstrumentationImpl 的loadClassAndCallPremain方法，在这个方法里会去调用 javaagent 中 MANIFEST.MF 里指定的Premain-Class 类的 premain 方法 ；

3. 解析 javaagent 中 MANIFEST.MF 文件的参数，并根据这些参数来设置 JPLISAgent 里的一些内容

#### 运行时加载instrument agent过程：

通过 JVM 的attach机制来请求目标 JVM 加载对应的agent，过程大致如下：

1. 创建并初始化JPLISAgent；

2. 解析 javaagent 里 MANIFEST.MF 里的参数；

3. 创建 InstrumentationImpl 对象；

4. 监听 ClassFileLoadHook 事件；

5. 调用 InstrumentationImpl 的loadClassAndCallAgentmain方法，在这个方法里会去调用javaagent里 MANIFEST.MF 里指定的Agent-Class类的agentmain方法。