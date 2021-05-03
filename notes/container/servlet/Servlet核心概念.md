### servlet 概述

Servlet 是一个基于Java技术的Web组件，由容器管理，生成动态内容。像其他基于java技术的组件一样，Servlet 是与平台无关的java类格式，它们被编译为与具体平台无关的字节码，可以被基于
java技术的Web Server 动态加载并运行。 容器（servlet 引擎）是web 服务器为支持 Servlet 功能扩展的部分。客户端通过Servlet 容器实现请求/应答模型与Servlet交互。

Servlet 容器是 Web Server 或者Application Server 的一部分，其提供基于请求/响应模型的网络服务，解码基于MIME的请求，并且格式化基于MIME的响应。Servlet容器也包含了管理Servlet生命周期的能力，
Servlet 是运行在Servlet容器内的。Servlet容器可以嵌入宿主Server中，或者通过Web Server的本地扩展API单独作为附加组件安装。Servlet容器也可能内嵌活安装到包含Web功能的Application Server中。


所有Servlet容器必须支持基于HTTP 协议的请求/响应模型，并且可以选择性支持基于HTTPS协议的请求/应答模型。容器必须实现的HTTP协议的请求/应答模型。容器必须实现的HTTP协议版本包含HTTP/1.0和HTTP/1.1

#### servlet 同步模型

[![gmuF8P.md.png](https://z3.ax1x.com/2021/05/03/gmuF8P.md.png)](https://imgtu.com/i/gmuF8P)

#### servlet 异步模型

[![gmufPI.md.png](https://z3.ax1x.com/2021/05/03/gmufPI.md.png)](https://imgtu.com/i/gmufPI)

具体处理请求响应的逻辑不再是Servlet 调用线程来做了，Servlet 开启异步处理后会立刻释放Servlet容器线程,具体对请求进行处理与响应的是业务线程池中的线程。

#### servlet 同步阻塞模型

[![gm70tP.png](https://z3.ax1x.com/2021/05/03/gm70tP.png)](https://imgtu.com/i/gm70tP)

- 从ServletInputStream 中读取请求体内容（请求头内容不在ServletInputStream中）

#### servlet 异步非阻塞模型

[![gmqgne.png](https://z3.ax1x.com/2021/05/03/gmqgne.png)](https://imgtu.com/i/gmqgne)

- 基于内核的能力，servlet3.1允许我们在ServletInputStream的上通过函数setReadListener注册一个监听器，该监听器在发现内核有数据时才会进行回调处理函数

- onDataAvailable()使用容器线程来执行，通过inputStream.isReady()发现数据准备好后，就使用容器线程来读取数据

- onAllDataRead()默认使用容器线程，也可以切换成用户线程来实现

##### servlet 异步请求流程

1. servlet 容器接收请求后会从容器线程池获取一个线程来执行具体Servlet的service方法，service方法内调用StartAsync开启异步处理，然后通过setReadListener注册一个readListener到ServletInputStream,最好释放容器线程

2. 当内核发现TCP 接收缓存有数据时，会回调注册的onDataAvailable()方法，这时使用的是容器线程，但是可以选择是否在方法内开启异步线程来对就绪线程进行读取，以便及时释放容器线程。

3. 当发现http的请求体已经读取完毕，会调用 onAllDataRead()方法，这个方法内使用业务线程池对请求进行处理，并把结果写会请求方

4. 