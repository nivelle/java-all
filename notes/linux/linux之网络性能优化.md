### linux 网络

#### 网络模型

[![ySjszt.png](https://s3.ax1x.com/2021/01/28/ySjszt.png)](https://imgchr.com/i/ySjszt)

- 传输层在应用程序数据前面加了TCP头

- 网络层在TCP数据包层前加了IP头

- 网络接口层，在IP数据包前后分别加了帧头和帧尾

[![ySvult.md.png](https://s3.ax1x.com/2021/01/28/ySvult.md.png)](https://imgchr.com/i/ySvult)

- 网卡硬中断只处理最核心的网卡数据读取或者发送

- 协议栈中的大部分逻辑，都在软中断中处理

#### 网络包接收流程