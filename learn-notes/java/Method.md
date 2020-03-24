

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
                     //计数器numInvocations，每调用一次方法+1,当比 ReflectionFactory.inflationThreshold(15)大的时候,用MethodAccessorGenerator创建一个MethodAccessor,并把之前的DelegatingMethodAccessorImpl引用替换为现在新创建的。下一次DelegatingMethodAccessorImpl就不会再交给NativeMethodAccessorImpl执行了，而是交给新生成的java字节码的MethodAccessor。MethodAccessorGenerator使用了asm字节码动态加载技术
                    
                     //否则使用nativeMethodAccessorImpl的invoke方法，执行效率比动态字节码高(少了C++代码和java代码的转换,但是生成动态字节码也比较耗时,所以要设置Dsun.reflect.inflationThreshold=15来设置阀值，调用超过15次再使用动态字节码技术)
                     return invoke0(this.method, var1, var2);
                 }
          
          ```
          
### 内联函数就是指函数在被调用的地方直接展开，编译器在调用时不用像一般函数那样，参数压栈，返回时参数出栈以及资源释放等，这样提高了程序执行速度。Java不支持直接声明为内联函数的，如果想让他内联，则是由编译器说了算，你只能够向编译器提出请求。