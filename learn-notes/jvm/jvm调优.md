
### 打印gc日志 -XX:+PrintGCDetails 

### -Djava.lang.Integer.IntegerCache.high=128;//控制integer缓存范围

### -Dsun.reflect.noInflation=true;//在反射调用一开始便会直接生成动态实现，而不会使用委派实现或者本地实现

### Dsun.reflect.inflationThreshold = 15;//当某个反射调用的调用次数在 15 之下时，采用本地实现；当达到 15 时，便开始动态生成字节码，并将委派实现的委派对象切换至动态实现，这个过程我们称之为 Inflation

### -XX:+TraceClassLoading  
