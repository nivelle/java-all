## SpringBoot注册DispatcherServlet

### DispatcherServletAutoConfiguration

#### 主要提供两个bean

- DispatcherServlet

- DispatcherServletRegistrationBean

 ```
 该bean的主要功能是将 DispatcherServlet 注册到 Servlet容器
 ```

#### 使用到的配置参数

- spring.http 的配置参数被加载到 bean HttpProperties

- spring.mvc 的配置参数被加载到 bean WebMvcProperties


 