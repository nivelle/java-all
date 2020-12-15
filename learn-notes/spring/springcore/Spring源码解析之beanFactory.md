### beanFactory 结构图

[![rm39uF.png](https://s3.ax1x.com/2020/12/13/rm39uF.png)](https://imgchr.com/i/rm39uF)

#### 简单容器

````
BeanFactory
--> HierarchicalBeanFactory
-----> ConfigurableBeanFactory
--------> ConfigurableListableBeanFactory
-----------> DefaultListableBeanFactory
````
[![rucby8.png](https://s3.ax1x.com/2020/12/14/rucby8.png)](https://imgchr.com/i/rucby8)



#### beanFactory容器
````
public interface BeanFactory {
    /**
     * 用户使用容器时可以使用转义符“&”来得到 FactoryBean 实例，用来区分通过容器获取的是 FactoryBean 产生的对象还是获取 FactoryBean 实例本身，
     * 例如：如果 myBean 是一个 FactoryBean，那么使用“&myBean”得到的是 FactoryBean 实例，而不是 myBean 这个由 FactoryBean 构造的实例
     */
    String FACTORY_BEAN_PREFIX = "&";

    /** 根据 bean 的名字获取对应的 bean 实例 */
    Object getBean(String name) throws BeansException;

    /** 根据 bean 的名字获取对应的 bean 实例，增加了对象类型检查 */
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

    /** 根据 bean 的名字获取对应的 bean 实例，可以指定构造函数的参数或者工厂方法的参数 */
    Object getBean(String name, Object... args) throws BeansException;

    /** 根据 bean 类型获取对应的 bean 实例 */
    <T> T getBean(Class<T> requiredType) throws BeansException;

    /** 根据 bean 类型获取对应的 bean 实例，可以指定构造函数的参数或者工厂方法的参数 */
    <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

    /** 判断容器是否持有指定名称的 bean 实例 */
    boolean containsBean(String name);

    /** 是不是单例 */
    boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

    /** 是不是原型对象 */
    boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

    /** 判断 name 对应的 bean 实例是不是指定 Class 类型 */
    boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

    /** 判断 name 对应的 bean 实例是不是指定 Class 类型 */
    boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

    /** 获取 bean 实例的 Class 对象 */
    Class<?> getType(String name) throws NoSuchBeanDefinitionException;

    /** 获取 bean 的所有别名，如果不存在则返回空 */
    String[] getAliases(String name);
}
````

##### HierarchicalBeanFactory

````
HierarchicalBeanFactory 译为中文是分层的 BeanFactory，它相对于 BeanFactory 增加了对父 BeanFactory 的获取。下层 IoC 容器（也叫子容器）可以通过 HierarchicalBeanFactory#getParentBeanFactory 方法访问父 IoC 容器，让容器的设计具备了层次性。

这种层次性增强了容器的扩展性和灵活性，我们可以通过编程的方式为一个已有的容器添加一个或多个子容器，从而实现一些特殊功能。

````

层次容器的一个特点就是子容器对于父容器来说是透明的，而子容器则能感知到父容器的存在。典型的应用场景就是 Spring MVC，控制层的 bean 位于子容器中，而业务层和持久层的 bean 则位于父容器中，这样的设计可以让控制层的 bean 访问业务层和持久层的 bean，反之则不行，从而在容器层面对三层软件结构设计提供约束。

##### ListableBeanFactory

ListableBeanFactory 中文译为可列举的 BeanFactory，对于 IoC 容器而言，bean 的定义和属性是可以列举的对象。

ListableBeanFactory 相对于 BeanFactory 增加了获取容器中 bean 的配置信息的若干方法，比如获取容器中 bean 的个数、获取容器中所有 bean 的名称列表、按照目标类型获取 bean 名称，以及检查容器中是否包含指定名称的 bean 等等。

##### AutowireCapableBeanFactory

AutowireCapableBeanFactory 提供了创建 bean 实例、自动注入、初始化，以及应用 bean 的后置处理器等功能。自动注入让配置变得更加简单，也让注解配置成为可能，Spring 目前提供了四种自动注入类型：byName,byType,constructor,autodetect

##### ConfigurableBeanFactory

ConfigurableBeanFactory 定义了配置 BeanFactory 的各种方法，增强了 IoC 容器的可定制性，包括设置类装载器、属性编辑器，以及容器初始化后置处理器等方法。

##### DefaultListableBeanFactory

DefaultListableBeanFactory 是一个非常重要的类，定义了 IoC 容器所应该具备的重要功能，是容器完整功能的基本实现。XmlBeanFactory 是一个典型的由该类派生出来的 BeanFactory，并且只是增加了加载 XML 配置资源的逻辑，而容器相关的特性则全部由 DefaultListableBeanFactory 来实现。XmlBeanFactory 类的实现如下：

````
public class XmlBeanFactory extends DefaultListableBeanFactory {

    private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

    public XmlBeanFactory(Resource resource) throws BeansException {
        this(resource, null);
    }

    public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
        super(parentBeanFactory);
        // 加载 XML 资源
        this.reader.loadBeanDefinitions(resource);
    }
}
````

### 高级容器

````
BeanFactory
--> ListableBeanFactory
-----> ApplicationContext
--------> ConfigurableApplicationContext & WebApplicationContext
````

[![ruoDBt.png](https://s3.ax1x.com/2020/12/15/ruoDBt.png)](https://imgchr.com/i/ruoDBt)

#### ConfigurableApplicationContext 和 WebApplicationContext 是直接实现 ApplicationContext 的两个接口

#### ConfigurableApplicationContext

ConfigurableApplicationContext 中主要增加了 ConfigurableApplicationContext#refresh 和 ConfigurableApplicationContext#close 两个方法，从而为应用上下文提供了启动、刷新和关闭的能力。其中 ConfigurableApplicationContext#refresh 方法是高级容器的核心方法，该方法概括了高级容器初始化的主要流程（包含简单容器的全部功能，以及高级容器扩展的功能）

#### WebApplicationContext

- WebApplicationContext 是为 WEB 应用定制的上下文类，基于 servlet 实现配置文件的加载和初始化工作。对于非 WEB 应用而言，bean 只有 singleton 和 prototype 两种作用域，而在 WebApplicationContext 中则新增了 request、session、globalSession，以及 application 四种作用域。

- WebApplicationContext 将整个应用上下文对象以属性的形式记录到 ServletContext 中，我们可以通过 WebApplicationContextUtils#getWebApplicationContext 工具方法从 ServletContext 对象中获取 WebApplicationContext 实例。

##### 为了支持这一特性，WebApplicationContext 类定义了一个常量：

````
ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT"
````

在初始化应用上下文时会以该常量为 key，将 WebApplicationContext 实例存放到 ServletContext 的属性列表中。当我们调用 WebApplicationContextUtils#getWebApplicationContext 工具方法时，本质上是在调用 ServletContext#getAttribute 方法，不过 Spring 会对获取的结果做一些校验工作。

