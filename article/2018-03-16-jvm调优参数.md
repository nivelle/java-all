---
layout: post
title:  "jvm调优参数"
date:   2018-03-16 01:06:05
categories: jvm
tags: jvm参数调优
excerpt: jvm参数调优
---


* content
{:toc}


## ParNew收集器

#### -XX：SurvivorRatio

#### -XX：PretenureSizeThreshold

#### -XX：ParallelGCThreads参数来限制垃圾收集的线程数
     
## Parallel Scavenge

#### -XX：MaxGCPauseMillis     
“控制最大垃圾收集停顿时间”

#### -XX：GCTimeRatio
设置吞吐量大小

####-XX：+UseAdaptiveSizePolicy

“当这个参数打开之后，就不需要手工指定新生代的大小（-Xmn）、Eden与Survivor区的比例（-XX：SurvivorRatio）、晋升老年代对象年龄（-XX：PretenureSizeThreshold）等细节参数了，虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时间或者最大的吞吐量，这种调节方式称为GC自适应的调节策略”
