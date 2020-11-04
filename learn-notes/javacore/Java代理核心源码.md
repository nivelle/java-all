## public class Proxy implements java.io.Serializable 
## 静态方法创建代理对象

```
public static Object newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler h) throws IllegalArgumentException
    {
        ## 验证传入的InvocationHandler(调用处理器)是否为空
        Objects.requireNonNull(h);
        ## 克隆代理类实现的接口
        final Class<?>[] intfs = interfaces.clone();
        ## 获得安全管理器
        final SecurityManager sm = System.getSecurityManager();
        ## 检查创建代理类所需的权限
        if (sm != null) {
            checkProxyAccess(Reflection.getCallerClass(), loader, intfs);
        }

        /*
         * Look up or generate the designated proxy class.
         * ##通过类加载器和接口列表查找或者生成特定的代理类（如果缓存中存在，则直接获取） 
         */
        Class<?> cl = getProxyClass0(loader, intfs);

        /*
         * Invoke its constructor with the designated invocation handler.
         */
        try {
            ## 权限校验
            if (sm != null) {
                checkNewProxyPermission(Reflection.getCallerClass(), cl);
            }
            ## 获取参数类型是 InvocationHandler.class 的代理类构造函数
            final Constructor<?> cons = cl.getConstructor(constructorParams);
            final InvocationHandler ih = h;
            ## 如果代理类是不可访问的, 就使用特权将它的构造器设置为可访问
            if (!Modifier.isPublic(cl.getModifiers())) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        cons.setAccessible(true);
                        return null;
                    }
                });
            }
            ## 传入InvocationHandler实例去构造一个代理类的实例,所有代理类都继承自Proxy,而Proxy构造方法需要InvocationHandler实例作为参数
            return cons.newInstance(new Object[]{h});
        } catch (IllegalAccessException|InstantiationException e) {
            throw new InternalError(e.toString(), e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new InternalError(t.toString(), t);
            }
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString(), e);
        }
    }
    
```
### 查找或则生成代理类

```
private static Class<?> getProxyClass0(ClassLoader loader,Class<?>... interfaces) {
        if (interfaces.length > 65535) {
            throw new IllegalArgumentException("interface limit exceeded");
        }

        // If the proxy class defined by the given loader implementing
        // the given interfaces exists, this will simply return the cached copy;
        // otherwise, it will create the proxy class via the ProxyClassFactory
        ## 如果由实现给定接口的代理类存在，简单地返回缓存的副本否则将通过ProxyClassFactory创建代理类
        return proxyClassCache.get(loader, interfaces);
    }

```

### 代理类创建工厂

```
private static final class ProxyClassFactory implements BiFunction<ClassLoader, Class<?>[], Class<?>>{
        // prefix for all proxy class names(代理类名称前缀)
        private static final String proxyClassNamePrefix = "$Proxy";
        // next number to use for generation of unique proxy class names
        ## 用原子类来生成代理类的序号, 保证序号唯一
        private static final AtomicLong nextUniqueNumber = new AtomicLong();

        @Override
        public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {

            Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
            for (Class<?> intf : interfaces) {
                /*
                 * Verify that the class loader resolves the name of this
                 * interface to the same Class object.
                 */
                Class<?> interfaceClass = null;
                try {
                    interfaceClass = Class.forName(intf.getName(), false, loader);
                } catch (ClassNotFoundException e) {
                }
                ## intf 是否可以由指定的类加载进行加载,如果不能加载则抛出异常
                if (interfaceClass != intf) {
                    throw new IllegalArgumentException(intf + " is not visible from class loader");
                }
                /*
                 * Verify that the Class object actually represents an
                 * interface.
                 */
                 ## intf是否是一个接口,如果不是接口则抛出异常。
                if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException(
                        interfaceClass.getName() + " is not an interface");
                }
                /*
                 * Verify that this interface is not a duplicate.
                 */
                ## intf在数组中是否有重复,如果重复抛出异常
                if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                    throw new IllegalArgumentException("repeated interface: " + interfaceClass.getName());
                }
            }
            // package to define proxy class in 生成代理类的包名
            String proxyPkg = null;     
            ## 代理类的访问标志, 默认是 public final
            int accessFlags = Modifier.PUBLIC | Modifier.FINAL;

            /*
             * Record the package of a non-public proxy interface so that the
             * proxy class will be defined in the same package.  Verify that
             * all non-public proxy interfaces are in the same package.
             */
             ## 验证所有非公共代理接口都在同一个包中
            for (Class<?> intf : interfaces) {
                ## 获取接口的访问标志
                int flags = intf.getModifiers();
                ## 如果接口的访问标志不是public, 那么生成代理类的包名和接口包名相同
                if (!Modifier.isPublic(flags)) {
                    ## 生成的代理类的访问标志设置为final
                    accessFlags = Modifier.FINAL;
                    String name = intf.getName();
                    int n = name.lastIndexOf('.');
                    String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                    if (proxyPkg == null) {
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        throw new IllegalArgumentException("non-public interfaces from different packages");
                    }
                }
            }

            if (proxyPkg == null) {
                // if no non-public proxy interfaces, use com.sun.proxy package
                ## package 如果没有非公共代理接口,那生成的代理类都放到默认的包下: com.sun.proxy
                proxyPkg = ReflectUtil.PROXY_PACKAGE + ".";
            }

            /*
             * Choose a name for the proxy class to generate.
             */
            long num = nextUniqueNumber.getAndIncrement();
            ## 生成代理类的全限定名, 包名+前缀+序号, 例如：com.sun.proxy.$Proxy0
            String proxyName = proxyPkg + proxyClassNamePrefix + num;

            /*
             * Generate the specified proxy class.
             */
             ## 这里是核心, 用 ProxyGenerator 来生成字节码, 该类放在sun.misc包下
            byte[] proxyClassFile = ProxyGenerator.generateProxyClass(proxyName, interfaces, accessFlags);
            try {
                ## 根据二进制文件生成相应的Class实例
                return defineClass0(loader, proxyName,proxyClassFile, 0, proxyClassFile.length);
            } catch (ClassFormatError e) {
                /*
                 * A ClassFormatError here means that (barring bugs in the
                 * proxy class generation code) there was some other
                 * invalid aspect of the arguments supplied to the proxy
                 * class creation (such as virtual machine limitations
                 * exceeded).
                 */
                throw new IllegalArgumentException(e.toString());
            }
        }
    }

```

### ProxyGenerator.generateProxyClass //生成代理类核心代码,并写入磁盘

```
@param var0 : 代理类名
@param var1 : 代理类接口数组
@param var2 : 访问限制 public 或则 final

public static byte[] generateProxyClass(final String var0, Class<?>[] var1, int var2) {
        //构造ProxyGenerator对象
        ProxyGenerator var3 = new ProxyGenerator(var0, var1, var2);
        //核心代码，生成代理类字节码文件
        final byte[] var4 = var3.generateClassFile();
        //如果需要保存生成的字节码文件，则将字节码文件写入磁盘
        if(saveGeneratedFiles) {
            AccessController.doPrivileged(new PrivilegedAction() {
                public Void run() {
                    try {
                        int var1 = var0.lastIndexOf(46);
                        Path var2;
                        //生成存储路径
                        if(var1 > 0) {
                            Path var3 = Paths.get(var0.substring(0, var1).replace('.', File.separatorChar), new String[0]);
                            Files.createDirectories(var3, new FileAttribute[0]);
                            var2 = var3.resolve(var0.substring(var1 + 1, var0.length()) + ".class");
                        } else {
                            var2 = Paths.get(var0 + ".class", new String[0]);
                        }
                        //将字节码文件写入磁盘
                        Files.write(var2, var4, new OpenOption[0]);
                        return null;
                    } catch (IOException var4x) {
                        throw new InternalError("I/O exception saving generated file: " + var4x);
                    }
                }
            });
        }
        //返回字节码文件
        return var4;
    }

```

### ProxyGenerator.generateClassFile
### 生成代理类字节码文件,并返回字节码数组
```
private byte[] generateClassFile() {
        //1、将所有的方法组装成ProxyMethod对象
        //首先为代理类生成toString, hashCode, equals等代理方法
        this.addProxyMethod(hashCodeMethod, Object.class);
        this.addProxyMethod(equalsMethod, Object.class);
        this.addProxyMethod(toStringMethod, Object.class);
        Class[] var1 = this.interfaces;
        int var2 = var1.length;

        int var3;
        Class var4;
        //遍历每一个接口的每一个方法, 并生成ProxyMethod对象
        for(var3 = 0; var3 < var2; ++var3) {
            var4 = var1[var3];
            Method[] var5 = var4.getMethods();
            int var6 = var5.length;
            for(int var7 = 0; var7 < var6; ++var7) {
                Method var8 = var5[var7];
                this.addProxyMethod(var8, var4);
            }
        }
        Iterator var11 = this.proxyMethods.values().iterator();
        List var12;
        while(var11.hasNext()) {
            var12 = (List)var11.next();
            //校验返回类型
            checkReturnTypes(var12);
        }
        //2、组装要生成的 class文件的所有的字段信息和方法信息
        Iterator var15;
        try {
            //添加构造器方法
            this.methods.add(this.generateConstructor());
            var11 = this.proxyMethods.values().iterator();
            //遍历缓存中的代理方法
            while(var11.hasNext()) {
                var12 = (List)var11.next();
                var15 = var12.iterator();

                while(var15.hasNext()) {
                    ProxyGenerator.ProxyMethod var16 = (ProxyGenerator.ProxyMethod)var15.next();
                    //添加代理类的静态字段, 例如:private static Method m1;
                    this.fields.add(new ProxyGenerator.FieldInfo(var16.methodFieldName, "Ljava/lang/reflect/Method;", 10));
                    //添加代理类的代理方法
                    this.methods.add(var16.generateMethod());
                }
            }

            //添加代理类的静态字段初始化方法
            this.methods.add(this.generateStaticInitializer());
        } catch (IOException var10) {
            throw new InternalError("unexpected I/O Exception", var10);
        }

        if(this.methods.size() > '\uffff') {
            throw new IllegalArgumentException("method limit exceeded");
        } else if(this.fields.size() > '\uffff') {
            throw new IllegalArgumentException("field limit exceeded");
        } else {
            //3、写入最终的class文件
            //验证常量池中存在代理类的全限定名
            this.cp.getClass(dotToSlash(this.className));
            //验证常量池中存在代理类父类的全限定名
            this.cp.getClass("java/lang/reflect/Proxy");
            var1 = this.interfaces;
            var2 = var1.length;

            //验证常量池存在代理类接口的全限定名
            for(var3 = 0; var3 < var2; ++var3) {
                var4 = var1[var3];
                this.cp.getClass(dotToSlash(var4.getName()));
            }

            //接下来要开始写入文件了,设置常量池只读
            this.cp.setReadOnly();
            ByteArrayOutputStream var13 = new ByteArrayOutputStream();
            DataOutputStream var14 = new DataOutputStream(var13);

            try {
                //1.写入魔数
                var14.writeInt(-889275714);
                //2.写入次版本号
                var14.writeShort(0);
                //3.写入主版本号
                var14.writeShort(49);
                //4.写入常量池
                this.cp.write(var14);
                //5.写入访问修饰符
                var14.writeShort(this.accessFlags);
                //6.写入类索引
                var14.writeShort(this.cp.getClass(dotToSlash(this.className)));
                //7.写入父类索引, 生成的代理类都继承自Proxy
                var14.writeShort(this.cp.getClass("java/lang/reflect/Proxy"));
                //8.写入接口计数值
                var14.writeShort(this.interfaces.length);
                
                Class[] var17 = this.interfaces;
                int var18 = var17.length;

                //9.写入接口集合
                for(int var19 = 0; var19 < var18; ++var19) {
                    Class var22 = var17[var19];
                    var14.writeShort(this.cp.getClass(dotToSlash(var22.getName())));
                }
                //10.写入字段计数值
                var14.writeShort(this.fields.size());
                var15 = this.fields.iterator();
                //11.写入字段集合 
                while(var15.hasNext()) {
                    ProxyGenerator.FieldInfo var20 = (ProxyGenerator.FieldInfo)var15.next();
                    var20.write(var14);
                }
                //12.写入方法计数值
                var14.writeShort(this.methods.size());
                var15 = this.methods.iterator();
                //13.写入方法集合
                while(var15.hasNext()) {
                    ProxyGenerator.MethodInfo var21 = (ProxyGenerator.MethodInfo)var15.next();
                    var21.write(var14);
                }
                 //14.写入属性计数值, 代理类class文件没有属性所以为0
                var14.writeShort(0);
                //转换成二进制数组输出
                return var13.toByteArray();
            } catch (IOException var9) {
                throw new InternalError("unexpected I/O Exception", var9);
            }
        }
    }

generateClassFile

```

### 代理类字节码文件分析 

```
package com.sun.proxy;

import com.doubibi.framework.util.proxy.HelloService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

public final class $Proxy0 extends Proxy
  implements HelloService
{
  //equals方法
  private static Method m1;
  //HelloService 的sayHello方法
  private static Method m3;
  //toString方法
  private static Method m2;
  //hashCode方法
  private static Method m0;

  //构造方法
  public $Proxy0(InvocationHandler paramInvocationHandler)
    throws 
  {
    super(paramInvocationHandler);
  }

  public final boolean equals(Object paramObject)
    throws 
  {
    try
    {
      return ((Boolean)this.h.invoke(this, m1, new Object[] { paramObject })).booleanValue();
    }
    catch (Error|RuntimeException localError)
    {
      throw localError;
    }
    catch (Throwable localThrowable)
    {
      throw new UndeclaredThrowableException(localThrowable);
    }
  }

  //调用了invocationHandler的invoke方法，invoke执行了HelloService 的sayHello方法
  public final void sayHello()
    throws 
  {
    try
    {
      this.h.invoke(this, m3, null);
      return;
    }
    catch (Error|RuntimeException localError)
    {
      throw localError;
    }
    catch (Throwable localThrowable)
    {
      throw new UndeclaredThrowableException(localThrowable);
    }
  }

  public final String toString()
    throws 
  {
    try
    {
      return (String)this.h.invoke(this, m2, null);
    }
    catch (Error|RuntimeException localError)
    {
      throw localError;
    }
    catch (Throwable localThrowable)
    {
      throw new UndeclaredThrowableException(localThrowable);
    }
  }

  public final int hashCode()
    throws 
  {
    try
    {
      return ((Integer)this.h.invoke(this, m0, null)).intValue();
    }
    catch (Error|RuntimeException localError)
    {
      throw localError;
    }
    catch (Throwable localThrowable)
    {
      throw new UndeclaredThrowableException(localThrowable);
    }
  }

  static
  {
    try
    {
      m1 = Class.forName("java.lang.Object").getMethod("equals", new Class[] { Class.forName("java.lang.Object") });
      m3 = Class.forName("com.doubibi.framework.util.proxy.HelloService").getMethod("sayHello", new Class[0]);
      m2 = Class.forName("java.lang.Object").getMethod("toString", new Class[0]);
      m0 = Class.forName("java.lang.Object").getMethod("hashCode", new Class[0]);
      return;
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      throw new NoSuchMethodError(localNoSuchMethodException.getMessage());
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
      throw new NoClassDefFoundError(localClassNotFoundException.getMessage());
    }
  }
}

 $Proxy0


```