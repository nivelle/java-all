### Environment

#### Environment是Spring核心框架中定义的一个接口，用来表示整个应用运行时的环境。

- profile

1. 一个profile是一组Bean定义(Bean definition)的逻辑分组(logical group)

2. 这个分组，也就是这个profile，被赋予一个命名，就是这个profile的名字

3. 只有当一个profile处于active状态时，它对应的逻辑上组织在一起的这些Bean定义才会被注册到容器中。

4. Bean添加到profile可以通过XML定义方式或者注解方式

5. Environment对于profile机制所扮演的角色是用来指定哪些profile是当前活跃的/缺省活跃的

- property 属性

1. 一个应用的属性有很多来源: 属性文件(properties files),JVM系统属性，系统环境变量，JNDI，servlet上下文参数，临时属性对象等。

2. Environment对于property所扮演的角色是提供给使用者一个方便的服务接口用于(配置属性源;从属性源中获取属性)

#### 容器(ApplicationContext)所管理的bean如果想直接使用Environment对象访问profile状态或者获取属性，可以有两种方式

- 实现EnvironmentAware接口

- @Inject 或者 @Autowired注入一个 Environment对象

**绝大多数情况下，bean都不需要直接访问Environment对象，而是通过类似@Value注解的方式把属性值注入进来。**
 

[![D6ct74.jpg](https://s3.ax1x.com/2020/11/29/D6ct74.jpg)](https://imgchr.com/i/D6ct74)


接口/类	 | 介绍
---|---
PropertyResolver | 接口，抽象对属性源的访问，比如是否包含某个属性，读取属性，解析占位符，将读取到的属性转换成指定类型
Environment |接口，继承自PropertyResolver,对环境属性访问和default/active profile访问的抽象因为继承自PropertyResolver，所以它自然具备PropertyResolver提供的所有能力，对环境属性的访问也正是通过PropertyResolver定义的这些能力
ConfigurablePropertyResolver | 接口，为PropertyResolver接口抽象的属性源访问做了配置方面的增强。比如设置将属性值转换工具，指定占位符前缀后缀，遇到不可解析的嵌套的占位符怎么办等等
ConfigurableEnvironment	| 接口，在所继承的接口之上增加了设置defaut/active profile的能力，增加/删除环境对象中属性源的能力
ConfigurableWebEnvironment | 接口，向接口ConfigurableEnvironment增强了根据Servlet上下文/配置初始化属性源的能力
AbstractEnvironment | Environment抽象基类，实现了ConfigurableEnvironment
StandardEnvironment | 实现类,针对标准Spring应用(非Web应用)环境,在AbstractEnvironment基础上提供了属性源systemEnvironment(来自System.getenv())和systemProperties(来自System.getProperties())
StandardServletEnvironment | 实现类,针对标准SpringServletWeb应用的环境，在StandardEnvironment的基础上增加了servletContextInitParams/servletConfigInitParams/jndiProperties三个属性源


#### StandardEnvironment 属性源 //针对Spring 非Web 应用

名称  | 介绍
---|---
spring.application.json | 系统环境变量spring.application.json/SPRING_APPLICATION_JSON指定的JSON格式的属性源1.可以不指定
systemProperties | 来自 system.getProperties()
systemEnvironment | 来自 system.getenv()
random | 来自一个Random对象,用来生成随机int/long/uuid
applicationConfig: [classpath:/application-dev1.yml | 属性spring.profiles.active指定的配置文件 1.可以不指定，也可以指定多个 2.后读进来的优先级较高3.可以是yml/yaml/properties/xml等文件类型 
applicationConfig: [classpath:/application-common1.yml] | 属性spring.profiles.active指定的配置文件 1.可以不指定，也可以指定多个 2.后读进来的优先级较高3.可以是yml/yaml/properties/xml等文件类型 
applicationConfig: [classpath:/application.yml]	 | 缺省配置文件可以是yml/yaml/properties/xml等文件类型

**注意: 上表中，各个属性源行号越小优先级越高**


#### StandardServletEnvironment

StandardServletEnvironment继承自StandardEnvironment,它往环境中增加了来自Servlet Web环境的属性源，并将这些属性源放在了StandardEnvironment中那些属性源之前，也就是使之有了更高优先级

名称  | 介绍
---|---
server.ports	 | 启动过程中获取嵌入式Servlet Web容器所监听的端口动态生成的一个属性源:properties={local.server.port=8080}1.针对Springboot Servlet Web应用的情况
servletConfigInitParams	 | 来自 ServletConfig的属性源ServletConfigPropertySource
servletContextInitParams	 | 来自 ServletContext的属性源ServletContextPropertySource
jndiProperties	 | 如果使用了jndi环境的话会添加该属性源JndiPropertySource

### PropertySource

对于各种基于"名称/值"对(key/value pair)的属性源,Spring将其抽象成了抽象泛型类PropertySource<T>。底层的属性源T可以是容纳属性信息的任意类型，

- java.util.Properties,
- java.util.Map,
- ServletContext,
- ServletConfig对象,
- 命令行参数CommandLineArgs对象。

类PropertySource的方法getSource()用于获取底层的属性源对象T。顶层的属性源对象经过PropertySource封装，从而具有统一的访问方式。

名称  | 介绍
---|---
RandomValuePropertySource	 | 封装一个random对象为属性源，用于获取int,long,uuid随机数
MapPropertySource	 | 封装一个Map<String,Object>对象为属性源
ServletConfigPropertySource	 | 封装一个ServletConfig对象为属性源
ServletContextPropertySource	 | 封装一个ServletContext对象为属性源
SystemEnvironmentPropertySource | 继承自MapPropertySource，被StandardEnvironment用于将System.getenv()封装成一个属性源在获取属性的属性名上针对不同环境做了处理，比如getProperty("foo.bar")会匹配"foo.bar",“foo_bar”,“FOO.BAR"或者"FOO_BAR”,如果想获取的属性名称中含有-，会被当作_处理
SimpleCommandLinePropertySource | 将命令行参数字符串数组转换成一个 CommandLineArgs对象，然后封装成一个属性源比如一个springboot应用SpringApplication启动时，如果提供了命令行参数，他们就会被封装成一个SimpleCommandLinePropertySource对象放到上下文环境中去。 
PropertiesPropertySource	| 继承自MapPropertySource,将一个java.util.Properties对象封装为属性源

[![D6Wzi4.png](https://s3.ax1x.com/2020/11/29/D6Wzi4.png)](https://imgchr.com/i/D6Wzi4)

````
package org.springframework.core.env;


/**
 * Abstract base class representing a source of name/value property pairs. The underlying
 * #getSource() source object may be of any type T that encapsulates
 * properties. Examples include java.util.Properties objects, java.util.Map
 * objects, ServletContext and ServletConfig objects (for access to init
 * parameters). Explore the PropertySource type hierarchy to see provided
 * implementations.
 * 
 * 抽象基类，用于表示一个名称/值属性对的来源，简称为属性源。底层的属性源对象的类型通过泛型类T指定。
 *
 * PropertySource objects are not typically used in isolation, but rather
 * through a PropertySources object, which aggregates property sources and in
 * conjunction with a PropertyResolver implementation that can perform
 * precedence-based searches across the set of PropertySources.
 *
 * PropertySource 对象通常并不孤立使用，而是将多个PropertySource对象封装成一个PropertySources
 * 对象来使用。另外还会有一个PropertyResolver属性解析器工作在PropertySources对象上，基于特定的优先级，
 * 来访问这些属性源对象中的属性。
 * 
 * PropertySource identity is determined not based on the content of
 * encapsulated properties, but rather based on the #getName() name of the
 * PropertySource alone. This is useful for manipulating PropertySource
 * objects when in collection contexts. See operations in MutablePropertySources
 * as well as the #named(String) and #toString() methods for details.
 * 
 * PropertySource有一个唯一标识id，这个唯一标识id不是基于所封装的属性内容，而是基于指定给
 * 这个PropertySource对象的名称属性#getName()。
 *
 * Note that when working with
 * org.springframework.context.annotation.Configuration Configuration classes that
 * the @org.springframework.context.annotation.PropertySource PropertySource
 * annotation provides a convenient and declarative way of adding property sources to the
 * enclosing Environment.
 *
 * @author Chris Beams
 * @since 3.1
 * @param <T> the source type
 * @see PropertySources
 * @see PropertyResolver
 * @see PropertySourcesPropertyResolver
 * @see MutablePropertySources
 * @see org.springframework.context.annotation.PropertySource
 */
public abstract class PropertySource<T> {

	protected final Log logger = LogFactory.getLog(getClass());

	protected final String name;

	protected final T source;


	/**
	 * Create a new PropertySource with the given name and source object.
	 * 将某个T类型的底层属性源source对象封装成一个特定名称为name的PropertySource对象
	 */
	public PropertySource(String name, T source) {
		Assert.hasText(name, "Property source name must contain at least one character");
		Assert.notNull(source, "Property source must not be null");
		this.name = name;
		this.source = source;
	}

	/**
	 * Create a new PropertySource with the given name and with a new
	 * Object instance as the underlying source.
	 * Often useful in testing scenarios when creating anonymous implementations
	 * that never query an actual source but rather return hard-coded values.
	 * 使用一个空对象构建一个指定名称为name的PropertySource对象。通常用于测试目的。
	 */
	@SuppressWarnings("unchecked")
	public PropertySource(String name) {
		this(name, (T) new Object());
	}


	/**
	 * Return the name of this PropertySource.获取属性源名称
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return the underlying source object for this PropertySource.
	 * 获取所封装的底层属性源对象，类型为泛型T。
	 */
	public T getSource() {
		return this.source;
	}

	/**
	 * Return whether this PropertySource contains the given name.
	 * This implementation simply checks for a null return value
	 * from #getProperty(String). Subclasses may wish to implement
	 * a more efficient algorithm if possible.
	 * 查看指定名称为name的属性是否被当前PropertySource对象包含，
	 * 判断方法：尝试获取该名称的属性值，如果值不为null认为是包含；否则认为是不包含。
	 * 继承类可以提供不同的判断方法和实现。
	 * @param name the property name to find
	 */
	public boolean containsProperty(String name) {
		return (getProperty(name) != null);
	}

	/**
	 * Return the value associated with the given name,
	 * or null if not found.
	 * 获取指定名称为name的属性的值，如果该属性不存在于该PropertySource对象，返回null
	 * @param name the property to find
	 * @see PropertyResolver#getRequiredProperty(String)
	 */
	@Nullable
	public abstract Object getProperty(String name);

}

````

### PropertyResolver //属性解析

Spring 使用接口PropertyResolver抽象了从底层来源获取属性的基本功能，和解析${...}这样的占位符的功能。

#### 接口定义

- boolean containsProperty(String key) – 是否包含某个属性

- String getProperty(String key) – 获取指定属性的值(字符串类型),不存在的话返回null

- String getProperty(String key, String defaultValue) – 获取指定属性的值(字符串类型),不存在的话返回defaultValue

- T getProperty(String key, Class<T> targetType,T defaultValue) – 获取指定属性的值(指定类型),不存在的话返回defaultValue

- String getRequiredProperty(String key) – 获取指定属性的值(字符串类型),不存在的话抛出异常IllegalStateException

- String getRequiredProperty(String key, Class<T> targetType) – 获取指定属性的值(指定类型),不存在的话抛出异常IllegalStateException

- String resolvePlaceholders(String text) – 解析text中的${...}占位符,忽略不能解析的占位符

- String resolveRequiredPlaceholders(String text) – 解析text中的${...}占位符,如果占位符不能被解析则抛出异常IllegalArgumentException

### PropertySourcesPropertyResolver 从PropertySources中解析属性

- Spring框架将某个属性源抽象成了类PropertySource，又将多个属性源PropertySource组合抽象为接口PropertySources。

- 对某个PropertySource对象中属性的解析，抽象成了接口PropertyResolver,而类PropertySourcesPropertyResolver则是Spring用于解析一个PropertySources对象中属性的工具类。

- Spring应用Environment对象中对其PropertySources对象的属性解析，就是通过这样一个对象。

````
// Spring Environment 实现类的抽象基类 AbstractEnvironment代码片段
private final ConfigurablePropertyResolver propertyResolver = new PropertySourcesPropertyResolver(this.propertySources);
````


````
package org.springframework.core.env;

import org.springframework.lang.Nullable;

/**
 * PropertyResolver implementation that resolves property values against
 * an underlying set of PropertySources.
 * 一个PropertyResolver实现类，用于解析一个PropertySources对象中的属性源集合中的属性。
 * 
 * 这里PropertySourcesPropertyResolver继承自基类AbstractPropertyResolver，
 * AbstractPropertyResolver提供了很多方法实现，不过这里不做过多解析，而仅仅关注
 * 如何获取一个属性的值的逻辑。
 * 
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see PropertySource
 * @see PropertySources
 * @see AbstractEnvironment
 */
public class PropertySourcesPropertyResolver extends AbstractPropertyResolver {
	// PropertySources形式存在的属性源集合，该工具的工作对象
	@Nullable
	private final PropertySources propertySources;


	/**
	 * Create a new resolver against the given property sources.
	 * @param propertySources the set of PropertySource objects to use
	 */
	public PropertySourcesPropertyResolver(@Nullable PropertySources propertySources) {
		this.propertySources = propertySources;
	}

	// 查看是否包含某个指定名称的属性，判断方法：
	// 1. 底层任何一个属性源包含该属性的话就认为是包含；
	// 2. 否则认为是不包含。
	@Override
	public boolean containsProperty(String key) {
		if (this.propertySources != null) {
			for (PropertySource<?> propertySource : this.propertySources) {
				if (propertySource.containsProperty(key)) {
					return true;
				}
			}
		}
		return false;
	}

	// 获取某个指定名称的属性的值，如果该属性不被包含的话，返回null
	// 不管该属性的值是什么类型，将它转换成字符串类型
	@Override
	@Nullable
	public String getProperty(String key) {
		return getProperty(key, String.class, true);
	}

	// 获取某个指定名称的属性的值，并将其转换成指定的类型，如果该属性不被包含的话，返回null
	@Override
	@Nullable
	public <T> T getProperty(String key, Class<T> targetValueType) {
		return getProperty(key, targetValueType, true);
	}
	
	// 获取某个指定名称的属性的值，如果该属性不被包含的话，返回null
	// 不管该属性的值是什么类型，将它转换成字符串类型
	@Override
	@Nullable
	protected String getPropertyAsRawString(String key) {
		return getProperty(key, String.class, false);
	}
	// 获取某个指定名称的属性的值，并将其转换成指定的类型，如果该属性不被包含的话，返回null
	// resolveNestedPlaceholders参数指示是否要解析属性值中包含的占位符
	@Nullable
	protected <T> T getProperty(String key, Class<T> targetValueType, boolean resolveNestedPlaceholders) {
		if (this.propertySources != null) {
			// 遍历每个属性源，如果发现目标属性被某个属性源包含，则获取它的值并按要求做相应的处理然后返回处理
			// 后的值从这里使用for循环的方式来看，可以将属性源看作是一个List，索引较小的属性源先被访问，也就
			// 是说，索引较小的属性源具有较高优先级
			for (PropertySource<?> propertySource : this.propertySources) {
				if (logger.isTraceEnabled()) {
					logger.trace("Searching for key '" + key + "' in PropertySource '" +
							propertySource.getName() + "'");
				}
				Object value = propertySource.getProperty(key);
				if (value != null) {
					if (resolveNestedPlaceholders && value instanceof String) {
						// 解析值中的占位符
						value = resolveNestedPlaceholders((String) value);
					}
					logKeyFound(key, propertySource, value);
					// 根据要求做相应的类型转换然后返回转换后的值
					return convertValueIfNecessary(value, targetValueType);
				}
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Could not find key '" + key + "' in any property source");
		}
		// 任何属性源中都不包含该属性，返回null
		return null;
	}

	/**
	 * Log the given key as found in the given PropertySource, resulting in
	 * the given value.
	 * The default implementation writes a debug log message with key and source.
	 * As of 4.3.3, this does not log the value anymore in order to avoid accidental
	 * logging of sensitive settings. Subclasses may override this method to change
	 * the log level and/or log message, including the property's value if desired.
	 * @param key the key found
	 * @param propertySource the PropertySource that the key has been found in
	 * @param value the corresponding value
	 * @since 4.3.1
	 */
	protected void logKeyFound(String key, PropertySource<?> propertySource, Object value) {
		if (logger.isDebugEnabled()) {
			logger.debug("Found key '" + key + "' in PropertySource '" + propertySource.getName() +
					"' with value of type " + value.getClass().getSimpleName());
		}
	}

}

````

### PropertyPlaceholderHelper

#### 将字符串里的占位符内容，用我们配置的properties里的替换。这个是一个单纯的类，没有继承没有实现，而且也没简单，没有依赖Spring框架其他的任何类。

````
 protected String parseStringValue(String value, PropertyPlaceholderHelper.PlaceholderResolver placeholderResolver, @Nullable Set<String> visitedPlaceholders) {
        //获取路径中占位符前缀的索引
        int startIndex = value.indexOf(this.placeholderPrefix);
        if (startIndex == -1) {
            return value;
        } else {
            StringBuilder result = new StringBuilder(value);

            while(startIndex != -1) {
                int endIndex = this.findPlaceholderEndIndex(result, startIndex);
                if (endIndex != -1) {
                    //截取前缀占位符和后缀占位符之间的字符串placeholder
                    String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
                    String originalPlaceholder = placeholder;
                    if (visitedPlaceholders == null) {
                        visitedPlaceholders = new HashSet(4);
                    }
                    if (!((Set)visitedPlaceholders).add(placeholder)) {
                        throw new IllegalArgumentException("Circular placeholder reference '" + placeholder + "' in property definitions");
                    }
                    //递归调用,继续解析placeholder
                    placeholder = this.parseStringValue(placeholder, placeholderResolver, (Set)visitedPlaceholders);
                    //获取placeholder的值
                    String propVal = placeholderResolver.resolvePlaceholder(placeholder);
                    if (propVal == null && this.valueSeparator != null) {
                        int separatorIndex = placeholder.indexOf(this.valueSeparator);
                        if (separatorIndex != -1) {
                            String actualPlaceholder = placeholder.substring(0, separatorIndex);
                            String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
                            propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                            if (propVal == null) {
                                propVal = defaultValue;
                            }
                        }
                    }

                    if (propVal != null) {
                        //对替换完成的value进行解析,防止properties的value值里也有占位符
                        propVal = this.parseStringValue(propVal, placeholderResolver, (Set)visitedPlaceholders);
                        result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Resolved placeholder '" + placeholder + "'");
                        }
                        //重新定位开始索引
                        startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                    } else {
                        if (!this.ignoreUnresolvablePlaceholders) {
                            throw new IllegalArgumentException("Could not resolve placeholder '" + placeholder + "' in value \"" + value + "\"");
                        }

                        startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
                    }

                    ((Set)visitedPlaceholders).remove(originalPlaceholder);
                } else {
                    startIndex = -1;
                }
            }

            return result.toString();
        }
    }

````

````
private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        //获取前缀后面一个字符的索引
		int index = startIndex + this.placeholderPrefix.length();
		int withinNestedPlaceholder = 0;
        //如果前缀后面还有字符的话
		while (index < buf.length()) {
            //判断源字符串在index处是否与后缀匹配
			if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
                //如果匹配到后缀,但此时前缀数量>后缀,则继续匹配后缀
				if (withinNestedPlaceholder > 0) {
					withinNestedPlaceholder--;
					index = index + this.placeholderSuffix.length();
				}
				else {
					return index;
				}
			}
			else if (StringUtils.substringMatch(buf, index, this.simplePrefix)) {
                //判断源字符串在index处是否与前缀匹配,若匹配,说明前缀后面还是前缀,则把前缀长度累加到index上,继续循环寻找后缀
                //withinNestedPlaceholder确保前缀和后缀成对出现后
				withinNestedPlaceholder++;
				index = index + this.simplePrefix.length();
			}
			else {
                //如果index出既不能和suffix又不能和simplePrefix匹配,则自增,继续循环
				index++;
			}
		}
		return -1;
	}

````