## invoke方法

```
public Object invoke(Object obj, Object... args)throws IllegalAccessException, IllegalArgumentException,InvocationTargetException{
        ## override表示此对象是否重写语言级访问检查。初始化为“false”
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

### acquireMethodAccessor
```
private MethodAccessor acquireMethodAccessor() {
        // First check to see if one has been created yet, and take it
        // if so
        MethodAccessor tmp = null;
        if (root != null) tmp = root.getMethodAccessor();
        if (tmp != null) {
            methodAccessor = tmp;
        } else {
            ## 否则就制造一个并传播到根部
            // Otherwise fabricate one and propagate it up to the root
            tmp = reflectionFactory.newMethodAccessor(this);
            setMethodAccessor(tmp);
        }

        return tmp;
    }
    
```

### private static native Object invoke0(Method var0, Object var1, Object[] var2);