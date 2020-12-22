### launcher 类加载启动器

- 拓展类加载器是Launcher类的一个内部类

- launcher构造函数

````
 public Launcher() {
        Launcher.ExtClassLoader extClassLoader;
        try {
            //获取拓展类加载器
            extClassLoader = Launcher.ExtClassLoader.getExtClassLoader();
        } catch (IOException var10) {
            throw new InternalError("Could not create extension class loader", var10);
        }
        try {
            //获取应用类加载器
            this.loader = Launcher.AppClassLoader.getAppClassLoader(extClassLoader);
        } catch (IOException var9) {
            throw new InternalError("Could not create application class loader", var9);
        }
        //把初始化的ApplicationLoader作为全局变量保存起来，并设置到当前线程的contextClassLoader
        //每个线程实例可以设置一个contextClassLoader
        // 这里的当前线程就是启动我们应用的线程,也就是执行main方法的线程
        Thread.currentThread().setContextClassLoader(this.loader);
        String var2 = System.getProperty("java.security.manager");
        if (var2 != null) {
            SecurityManager var3 = null;
            if (!"".equals(var2) && !"default".equals(var2)) {
                try {
                    //加载SecurityManager
                    var3 = (SecurityManager)this.loader.loadClass(var2).newInstance();
                } catch (IllegalAccessException var5) {
                } catch (InstantiationException var6) {
                } catch (ClassNotFoundException var7) {
                } catch (ClassCastException var8) {
                }
            } else {
                var3 = new SecurityManager();
            }

            if (var3 == null) {
                throw new InternalError("Could not create SecurityManager: " + var2);
            }

            System.setSecurityManager(var3);
        }

    }

````
####  ExtClassLoader //Launcher的静态内部类

````
public static Launcher.ExtClassLoader getExtClassLoader() throws IOException {
             //单例模式
            if (instance == null) {
                Class var0 = Launcher.ExtClassLoader.class;
                synchronized(Launcher.ExtClassLoader.class) {
                    if (instance == null) {
                        instance = createExtClassLoader();
                    }
                }
            }

            return instance;
        }
````

#### ApplicationClassLoader

````
//参数var0：ExtClassLoader
public static ClassLoader getAppClassLoader(final ClassLoader var0) throws IOException {
            final String var1 = System.getProperty("java.class.path");
            final File[] var2 = var1 == null ? new File[0] : Launcher.getClassPath(var1);
            return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction<Launcher.AppClassLoader>() {
                public Launcher.AppClassLoader run() {
                    URL[] var1x = var1 == null ? new URL[0] : Launcher.pathToURLs(var2);
                    return new Launcher.AppClassLoader(var1x, var0);
                }
            });
        }

````

### ClassLoader类加载器

#### 双亲委派模式的代码实现

````
//@param  name The <a href="#name">binary name</a> of the class
//@param  resolve If <tt>true</tt> then resolve the class
 protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            //查看缓存区指定类名(二进制)是否已经加载过，找到就不用重新加载
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        //如果父类加载器不为空，则委托给父类加载器区加载
                        c = parent.loadClass(name, false);
                    } else {
                        //如果没有父类，则委托给启动加载器区加载
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }

                if (c == null) {
                    // If still not found, then invoke findClass in order to find the class.
                    long t1 = System.nanoTime();
                    // 如果都没有找到，则通过自定义实现的findClass去查找并加载
                    // 鼓励重写findClass()方法而不是loadClass()方法来实现自定义类加载器
                    c = findClass(name);

                    // this is the defining class loader; record the stats
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                //是否需要在加载时进行解析;名字具有诱惑性，resolve其实是链接过程
                resolveClass(c);
            }
            return c;
        }
    }
````

##### findClass(String name)

- 自定义类加载器把自定义的类加载逻辑写在findClass()方法中

- 自定义的类加载逻辑写findClass()方法中，在loadClass()方法中调用，当loadClass()方法中父加载器加载失败后，则会调用自己的findClass()方法来完成类加载，这样也就可以保证自定类加载器也符合双亲委托模式

###### URLClassLoader 实现类
````
 protected Class<?> findClass(final String name)throws ClassNotFoundException
    {
        final Class<?> result;
        try {
            result = AccessController.doPrivileged(
                new PrivilegedExceptionAction<Class<?>>() {
                    public Class<?> run() throws ClassNotFoundException {
                        String path = name.replace('.', '/').concat(".class");
                        Resource res = ucp.getResource(path, false);
                        if (res != null) {
                            try {
                                return defineClass(name, res);
                            } catch (IOException e) {
                                throw new ClassNotFoundException(name, e);
                            }
                        } else {
                            return null;
                        }
                    }
                }, acc);
        } catch (java.security.PrivilegedActionException pae) {
            throw (ClassNotFoundException) pae.getException();
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        }
        return result;
    }
````
###### 实现类实现具体逻辑

- defineClass()方法是用来将byte字节留解析成JVM能够识别的class对象,通过这个方法不仅能够通过class文件实例化class对象,也可以通过其他方式实现实例化class对象,例如通过网络接收一个类的字节码，
然后转换为byte字节流创建对应的class对象，defineClass()方法通常与findClass()方法一起使用，一般情况下，在自定义类加载器时，会直接覆盖ClassLoader的findClass()方法并编写加载规则，取得要加载类的字节码后转换成流，然后调用defineClass()方法生成类
的Class对象。

- 自定义类加载器时，会直接覆盖ClassLoader的findClass()方法并编写加载规则，取得要加载类的字节码后转换成流，然后调用defineClass()方法生成类的Class对象

- JVM已经实现了对应的具体功能，解析对应的字节码，产生对应的内部数据结构放置到方法区，所以无需覆写，直接调用就可以了

````
protected Class<?> findClass(String name) throws ClassNotFoundException {
	  // 获取类的字节数组
      byte[] classData = getClassData(name);  
      if (classData == null) {
          throw new ClassNotFoundException();
      } else {
	      //使用defineClass生成class对象
          return defineClass(name, classData, 0, classData.length);
      }
  }
//创建 Class对象实例
 protected final Class<?> defineClass(String name, byte[] b, int off, int len,ProtectionDomain protectionDomain)throws ClassFormatError

    {
        protectionDomain = preDefineClass(name, protectionDomain);
        String source = defineClassSourceLocation(protectionDomain);
        Class<?> c = defineClass1(name, b, off, len, protectionDomain, source);
        postDefineClass(c, protectionDomain);
        return c;
    }
````
- 如果直接调用defineClass()方法生成类的Class对象,这个类的Class对象并没有解析(理解为链接阶段),其解析阶段需要等待初始化阶段进行。

- 如果使用 resolveClass()可以是类创建完成时同时也完成解析, 链接阶段主要是对字节码进行验证,为类变量分配内存并设置初始值同时将字节码文件中的符号引用转为直接引用.

#### 自定义类加载器

自定义类加载器一般都是继承自ClassLoader类，我们只需要重写findClass()

#### 命名空间

##### 每个类加载器都有自己的命名空间,命名空间由该加载器以及所有父加载器加载的类组成

- 在同一个命名空间中， 不会出现类的完整名字（包括类的包名）相同的两个类

- 在不同的命名空间中，有可能会出现类的完整名字相同的两个类

- 子加载器加载的类能看见父加载器的类，由父亲加载器加载的类不能看见子加载器加载的类

**在相同命名空间中，每个类只能被加载一次，反过来说就是一个类在不同的命名空间中是可以被加载多次的，而被加载多次的Class对象是互相独立的。**

#### 类加载机制

- 全盘负责：当一个类加载器负责加载某个class时，该class所依赖和引用的其他class也将由该类加载器负责载入，除非显示使用另外一个类加载器来载入

- 父类委托：先让父类加载器试图加载该类，只有在父类加载器无法加载该类时才从自己的类路径加载该类

- 缓存机制： 缓存机制将会保证所有加载过的Class都会被缓存，当程序中需要使用某个Class时，类加载器先从缓存区寻找该Class，只有缓存区不存在，系统才会读取该类对应的二进制数据，并将其转换成class对象，存入缓存区。这就是为什么修改了Class后，
必须重启JVM，程序的修改才会生效。

### 加载类的三种方式

- 静态加载：也就是通过new 关键字来创建实例对象

- 动态加载：也就是通过Class.forName()方法动态加载[反射加载类型]，然后调用类的newInstance()方法实例化对象

- 动态加载，通过类加载器loadClass()方法来加载类，然后调用类的newInstance()方法实例化对象

#### 区别：

- 第一种和第二种都使用的类加载器是相同的，都是当前类加载器(this.getClass.getCLassLoader());第三种则通过指定类加载器；

- 如果需要在当前类路径意外寻找类，则只能通过第三种，第三种方式加载的类与当前类分属不同的命名空间

- 第一种是静态加载，第二三是动态加载

#### 异常

- 静态类加载的时候如果在运行环境中找不到要初始化的类，抛出的是 NoClassDefFountError,在java异常提醒中是一个Error

- 动态加载的时候如果在运行环境中找不到要初始化的类，抛出 ClassNotFoundException，在java异常体系中是一个checked

### Class.forName

#### Class.forName()是一种获取Class对象的方法，而且是静态方法

````
Class.forName()是一个静态方法，同样可以用来加载类，Class.forName()返回与给定的字符串名称相关联类或接口的Class对象。注意这是一种获取Class对象的方法
````
#### 源码

````
 // initialize:指Class被loading后是不是必须被初始化
 // Class.forName(className)加载类时则已初始化。
 // 获得字符串参数中指定的类,并初始化该类
 public static Class<?> forName(String name, boolean initialize,ClassLoader loader)throws ClassNotFoundException
    {
        Class<?> caller = null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // Reflective call to get caller class is only needed if a security manager
            // is present.  Avoid the overhead of making this call otherwise.
            caller = Reflection.getCallerClass();
            if (sun.misc.VM.isSystemDomainLoader(loader)) {
                ClassLoader ccl = ClassLoader.getClassLoader(caller);
                if (!sun.misc.VM.isSystemDomainLoader(ccl)) {
                    sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
                }
            }
        }
        return forName0(name, initialize, loader, caller);
    }
````
- Class.forName():除了将类的.class文件加载到JVM中之外，还会对类进行解析，执行类中的static块

- ClassLoader.loadClass():只干一件事，就是将.class文件加载到jvm中,不会执行static中的内容,只有在newInstance才会去执行static块；将类属性赋值延迟到类初始化阶段;

- Class.forName(name,initialize,loader):带参函数也可控制是否加载static块。并且只有调用了newInstance()方法采用调用构造函数，创建类的对象

#### 类初始化和对象初始化

- 初始化类构造器：JVM会按照顺序收集类变量的赋值语句、静态代码块、最终组成类构造器由JVM执行

- 初始化对象构造器：JVM会按照收集成员变量的赋值语句、普通代码块、最后收集构造方法，将它们组成对象构造器，最终由JVM执行。如果没有检测或者收集到构造函数的代码，则将不会执行对象初始化方法。对象初始化方法一般在实例化类对象的时候执行

- 如果在初始化main方法所在类的时候遇到了其他类的初始化，那么就加载对应的类，加载完成之后返回。如果反复循环，最终返回main方法所在类

### 类加载器

- "定义类加载器"和"初始化类加载器"
- jvm为每个类加载器维护的一个"表"，这个表记录了所有以此类加载器为"初始类加载器"（而不是定义类加载器，所以一个类可以存在于很多的命名空间中）加载的类的列表
````
String类，bootstrap是"定义类加载器"，AppClassLoader、ExtClassloader都是String的初始类加载器
````
- 一个类，由不同的类加载器实例加载的话，会在方法区产生两个不同的类，彼此不可见，并且在堆中生成不同Class实例

- 所有通过正常双亲委派模式的类加载器加载的classpath下的和ext下的所有类在方法区都是同一个类，堆中的Class实例也是同一个

- java虚拟机出于安全，不会加载lib 下的陌生类

### class.forName()

````
//java.lang.Class.java  
publicstatic Class<?> forName(String className) throws ClassNotFoundException {  
    return forName0(className, true, ClassLoader.getCallerClassLoader());  
}  
  
//java.lang.ClassLoader.java  
// Returns the invoker's class loader, or null if none.  
static ClassLoader getCallerClassLoader() {  
    // 获取调用类（caller）的类型  
    Class caller = Reflection.getCallerClass(3);  
    // This can be null if the VM is requesting it  
    if (caller == null) {  
        return null;  
    }  
    // 调用java.lang.Class中本地方法获取加载该调用类（caller）的ClassLoader  
    return caller.getClassLoader0();  
}  
  
//java.lang.Class.java  
//虚拟机本地实现，获取当前类的类加载器，前面介绍的Class的getClassLoader()也使用此方法  
native ClassLoader getClassLoader0();  

````