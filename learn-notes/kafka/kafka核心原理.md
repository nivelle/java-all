### kafka

- 消息引擎系统

- 分布式流处理平台

### 核心脑图(极客时间：胡夕)

[![极客时间：胡夕](https://s3.ax1x.com/2020/12/24/r2ZZrR.jpg)](极客时间：胡夕)

#### 版本演进

- 0.7 版本：只有基础消息队列功能，无副本；不能线上使用

- 0.8 版本：增加了副本机制，新的producer API；建议使用0.8.2.2 版本；不建议使用0.8.2.0之后的producer API,bug多

- 0.9 版本：增加权限认证，新的consumer API ,Kafka Connect 功能；不建议使用Consumer API,bug 多

- 0.10 版本：引入kafka Streams 功能，bug修复；建议版本0.10.2.2 ；建议使用新版本consumer API

- 0.11 版本：producer API 幂等，事物API ，消息格式重构；建议版本 0.11.0.3 ;谨慎对待消息格式变化

- 1.0 和 2.0：kafka Streams 改进；建议版本2.0