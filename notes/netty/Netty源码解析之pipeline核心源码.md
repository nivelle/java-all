### ChannelPipeline 

[![gEmp24.png](https://z3.ax1x.com/2021/04/30/gEmp24.png)](https://imgtu.com/i/gEmp24)

- TailContext 实现接口: ChannelInboundHandler; HeadContext 实现接口:  ChannelOutboundHandler, ChannelInboundHandler

- Inbound 类似于事件回调(响应请求的事件)；outBound 类似于主动触发（发起请求的事件）

- 如果补获一个事件，并且想让这个事件继续传递下去，那么就需要调用Context对应的传播方法fireXXX()方法

#### ChannelInitializer
