- "Xms=2048m"
- "Xmx=2048m"

- "Xmn=1024m"
    
- "MetaspaceSize=128m"//MetaspaceSize表示metaspace首次使用不够而触发FGC的阈值，只对触发起作用，原因是：垃圾搜集器内部是根据变量_capacity_until_GC来判断metaspace区域是否达到阈值的

    
- "MaxMetaspaceSize=128m" //MaxMetaspaceSize用于设置metaspace区域的最大值
    
- "SurvivorRatio=6" //新生代中eden和S0/S1的比例
    
- "CMSInitiatingOccupancyFraction=70"