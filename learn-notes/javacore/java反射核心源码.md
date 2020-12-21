### 反射方法

- public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException

  - if (!override)
  
    - if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) //检查方法是否是public的
    
      - Class<?> caller = Reflection.getCallerClass();
      
      - checkAccess(caller, clazz, obj, modifiers);
      
    - MethodAccessor ma = methodAccessor; 
    
    - if (ma == null)
    
      - ma = acquireMethodAccessor();
      
    - return ma.invoke(obj, args);
    
      - Object invoke(Object var1, Object[] var2) throws IllegalArgumentException, InvocationTargetException;
      
      ### 子类实现(每个Method对象包含一个root对象，root对象里持有一个MethodAccessor对象。我们获得的Method独享相当于一个root对象的镜像，所有这类Method共享root里的MethodAccessor对象,这个对象由ReflectionFactory方法生成,ReflectionFactory对象在Method类中是static final的由native方法实例化)
          
        - 接口:MethodAccessor
        
        - 抽象类:MethodAccessorImpl
      
        - 子类:DelegatingMethodAccessorImpl
        
          ```
          public Object invoke(Object var1, Object[] var2) throws IllegalArgumentException, InvocationTargetException {
                  return this.delegate.invoke(var1, var2);
              }
              
          ```
        - 子类:NativeMethodAccessorImpl
        
          ```
          public Object invoke(Object var1, Object[] var2) throws IllegalArgumentException, InvocationTargetException {
                    
                     if (++this.numInvocations > ReflectionFactory.inflationThreshold() && !ReflectUtil.isVMAnonymousClass(this.method.getDeclaringClass())) {
                         MethodAccessorImpl var3 = (MethodAccessorImpl)(new MethodAccessorGenerator()).generateMethod(this.method.getDeclaringClass(), this.method.getName(), this.method.getParameterTypes(), this.method.getReturnType(), this.method.getExceptionTypes(), this.method.getModifiers());
                         this.parent.setDelegate(var3);
                     }
                     //计数器numInvocations，每调用一次方法+1,当比 ReflectionFactory.inflationThreshold(15)大的时候,
                     //用MethodAccessorGenerator创建一个MethodAccessor,并把之前的DelegatingMethodAccessorImpl引用替换为现在新创建的。
                     //下一次DelegatingMethodAccessorImpl就不会再交给NativeMethodAccessorImpl执行了，而是交给新生成的java字节码的MethodAccessor。
                     //MethodAccessorGenerator使用了asm字节码动态加载技术
                    
                     //否则使用nativeMethodAccessorImpl的invoke方法，执行效率比动态字节码高
                     (少了C++代码和java代码的转换,但是生成动态字节码也比较耗时,所以要设置Dsun.reflect.inflationThreshold=15来设置阀值，调用超过15次再使用动态字节码技术)
                     return invoke0(this.method, var1, var2);
                 }
          
          ```
          

### 方法分派

- 静态分派：静态方法，构造方法，私有方法以及父类方法（通过super调用的方法）都无法通过继承的方式被覆盖，因为在编译期就能确定其版本。我们称这类分派为静态分派，方法的重载是这种类型的典型场景。

- 编译期无法确定其接收者真实类型，编译期只能确定方法接收者的静态类型，根据静态类型确定所调用方法的签名，无法知道其运行期值的类型，具体调用哪个类的相应方法版本只有在运行期根据接收者的实际类型来确定。方法的重写是这种应用的典型场景

### 方法句柄

```
JSR-292是JVM为动态类型支持而出现的规范,在JAVA7中实现了这个规则,这个包的主要作用就在之前只能依赖符号引用来确定目标方法的基础上,增加了一种动态确定目标方法的机制,也就是方法句柄MethodHandler。

这有点类似于C++中的函数指针。从功能上讲,方法句柄类似于反射中的Method类,但两者之间有区别,方法句柄是轻量级的,我们从Method和MethodHandler的实现上可以看出来,Method的invoke方法会涉及到JAVA的安全访问检查,而方法句柄的所有invoke方法都是native方法,其性能优于反射.

```
- invokeVirtual:根据虚方法表调用虚方法（动态分派）

- invokeInterface:调用接口方法（动态分派）

- invokeSpecial:调用实例构造方法,私有方法,父类继承方法（静态分派）

- invokeStatic:调用静态方法（静态分派）

- invokeDynamic:动态方法绑定