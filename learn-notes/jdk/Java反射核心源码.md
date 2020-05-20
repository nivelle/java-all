## invoke方法

```
public Object invoke(Object obj, Object... args)throws IllegalAccessException, IllegalArgumentException,InvocationTargetException{
        ## override 表示此对象是否重写语言级访问检查,初始化为“false”
        if (!override) {
            ## 检查是否为public
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass();
                ## 判断caller是否有权限访问该方法
                checkAccess(caller, clazz, obj, modifiers);
            }
        }
        MethodAccessor ma = methodAccessor;// read volatile
        if (ma == null) {
            ma = acquireMethodAccessor();
        }
        return ma.invoke(obj, args);
 }

```

### acquireMethodAccessor 方法访问器
```
private MethodAccessor acquireMethodAccessor() {
        // First check to see if one has been created yet, and take it
        // if so
        MethodAccessor tmp = null;
        if (root != null) tmp = root.getMethodAccessor();
        if (tmp != null) {
            methodAccessor = tmp;
        } else {
            ## 否则就制造一个方法访问器并传播到根部
            // Otherwise fabricate one and propagate it up to the root
            tmp = reflectionFactory.newMethodAccessor(this);
            setMethodAccessor(tmp);
        }
        return tmp;
    }
    
```

### 最后是访问访问器来执行反射调用

 ```
 private static native Object invoke0(Method var0, Object var1, Object[] var2);
 
 ```