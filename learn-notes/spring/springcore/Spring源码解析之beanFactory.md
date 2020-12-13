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

#### 高级容器

````
BeanFactory
--> ListableBeanFactory
-----> ApplicationContext
--------> ConfigurableApplicationContext & WebApplicationContext
````

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