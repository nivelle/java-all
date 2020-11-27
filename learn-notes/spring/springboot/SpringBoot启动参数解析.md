### javaagent

#### 使用

````
-javaagent:<jarpath>[=<options>]

````

#### 作用
````
代理 (agent) 是在你的main方法前的一个拦截器 (interceptor)，也就是在main方法执行之前，执行agent的代码

agent的代码与你的main方法在同一个JVM中运行，并被同一个system classloader装载，被同一的安全策略 (security policy) 和上下文 (context) 所管理。

````

#### 实现

````
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