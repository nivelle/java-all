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