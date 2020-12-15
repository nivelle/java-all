### BeanDefinition

````
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor implements BeanDefinition, Cloneable {

    /** 对应的类 Class 对象 */
    private volatile Object beanClass;
    /** 作用域，对应 scope 属性 */
    private String scope = SCOPE_DEFAULT;
    /** 抽象类标识，对应 abstract 属性 */
    private boolean abstractFlag = false;
    /** 延迟加载标识，对应 lazy-init 属性 */
    private Boolean lazyInit;
    /** 自定装载类型，对应 autowire 配置 */
    private int autowireMode = AUTOWIRE_NO;
    //依赖检查
    private int dependencyCheck = DEPENDENCY_CHECK_NONE;
    /** 对应 depends-on 属性，表示一个 bean 实例化前置依赖另一个 bean */
    private String[] dependsOn;
    /** 对应 autowire-candidate 属性，设置为 false 时表示取消当前 bean 作为自动装配候选者的资格 */
    private boolean autowireCandidate = true;
    /** 对应 primary 属性，当自动装配存在多个候选者时，将当前 bean 作为首选 */
    private boolean primary = false;
    /** 对应 qualifier 属性 */
    private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>();
    /** 创建 bean 实例时的回调函数 */
    private Supplier<?> instanceSupplier;
    /** 非配置项，表示允许访问非公开的构造器和方法，由程序设置 */
    private boolean nonPublicAccessAllowed = true;
    /**
     * 非配置项，表示是否允许以宽松的模式解析构造函数，由程序设置
     *
     * 例如：如果设置为 true，则在下列情况时不会抛出异常（示例来源于《Spring 源码深度解析》）
     * interface ITest{}
     * class ITestImpl implements ITest {}
     * class Main {
     * Main(ITest i){}
     * Main(ITestImpl i){}
     * }
     */
    private boolean lenientConstructorResolution = true;
    /** 对应 factory-bean 属性 */
    private String factoryBeanName;
    /** 对应 factory-method 属性 */
    private String factoryMethodName;
    /** 构造函数注入属性，对应 <construct-arg/> 标签 */
    private ConstructorArgumentValues constructorArgumentValues;
    /** 记录 <property/> 属性集合 */
    private MutablePropertyValues propertyValues;
    /** 记录 <lookup-method/> 和 <replaced-method/> 标签配置 */
    private MethodOverrides methodOverrides = new MethodOverrides();
    /** 对应 init-method 属性 */
    private String initMethodName;
    /** 对应 destroy-method 属性 */
    private String destroyMethodName;
    /** 非配置项，是否执行 init-method，由程序设置 */
    private boolean enforceInitMethod = true;
    /** 非配置项，是否执行 destroy-method，由程序设置 */
    private boolean enforceDestroyMethod = true;
    /** 非配置项，表示 bean 是否是用户定义而不是程序定义的，创建 AOP 时为 true，由程序设置 */
    private boolean synthetic = false;
    /**
     * 非配置项，定义 bean 的应用场景，由程序设置，角色如下：
     * ROLE_APPLICATION：用户
     * ROLE_INFRASTRUCTURE：完全内部使用
     * ROLE_SUPPORT：某些复杂配置的一部分
     */
    private int role = BeanDefinition.ROLE_APPLICATION;
    /** 描述信息，对应 description 标签 */
    private String description;
    /** 定义的资源 */
    private Resource resource;

}

````
#### autowireMode 自动注入

- int AUTOWIRE_NO = 0;//不会对当前Bean进行外部类的注入，但是BeanFactoryAware和annotation-driven仍然会被应用就是说Bean里面加了@Autowired的@Resource这类的依然会有作用

- int AUTOWIRE_BY_NAME = 1;//把与Bean的属性具有相同名字的其他Bean自动装配到Bean的对应属性中

- int AUTOWIRE_BY_TYPE = 2;//把与Bean的属性具有相同类型的其他Bean自动装配到Bean的对应属性中。

- int AUTOWIRE_CONSTRUCTOR = 3;//把与Bean的构造器入参具有相同类型的其他Bean自动装配到Bean构造器的对应入参中。值的注意的是，具有相同类型的其他Bean这句话说明它在查找入参的时候，还是通过Bean的类型来确定

- int AUTOWIRE_AUTODETECT = 4;//它首先会尝试使用constructor进行自动装配，如果失败再尝试使用byType。不过，它在Spring3.0之后已经被标记为@Deprecated。

#### dependencyCheck 依赖检查

##### 在自动装配中，因为是隐式的，所以开发人员很难看出Bean的每个属性是否都设定完成，这时就要借助于依赖检查来实现查看Bean的每个属性是否都设定完成的功能，依赖检查，对应 dependency-check 属性：

- DEPENDENCY_CHECK_NONE = 0;//不进行依赖检查

- DEPENDENCY_CHECK_OBJECTS = 1;//object模式指的是对依赖的对象进行依赖检查.

- DEPENDENCY_CHECK_SIMPLE = 2;// simple模式是指对基本类型，字符串和集合进行依赖检查;isSimpleProperty

- DEPENDENCY_CHECK_ALL = 3;// all模式指的是对全部属性进行依赖检查.
  
#### Autowired 注解

从Spring2.5开始，开始支持使用注解来自动装配Bean的属性。它允许更细粒度的自动装配，我们可以选择性的标注某一个属性来对其应用自动装配。

Spring支持几种不同的应用于自动装配的注解:

- Spring自带的@Autowired 注解
- JSR-330的@Inject注解。
- JSR-250的@Resource注解。

##### 强制性

- 默认情况下，它具有强制契约特性，其所标注的属性必须是可装配的。如果没有Bean可以装配到Autowired所标注的属性或参数中，那么你会看到NoSuchBeanDefinitionException的异常信息;

- 如果取消强制性，则 @Autowired(required=false)

##### 装配策略

- 默认是按照类型装配，即byType，仅仅有一个类型匹配上

- 按照名称装配
  
**按照类型匹配可能会查询到多个实例,可以加注解以此规避。比如@qulifier、@Primary等;也可以在注入的时候，就可以把属性名称定义为Bean实现类的名称**

**AutowiredAnnotationBeanPostProcessor在处理依赖注入时，从bean工厂中去获取，首先是根据字段的类型去找符合条件的bean，若得到的bean有多个，则找出有@Primary注解修饰的bean，若都没有,则退化成@Resource注解的功能，即根据字段名去寻找bean，若都没有，则会抛出找到多个bean的异常**

**装配是在   InstantiationAwareBeanPostProcessors**


````
        // 获取匹配类型的bean实例
        Map<String, Object> matchingBeans = this.findAutowireCandidates(beanName, type, descriptor);
        if (matchingBeans.isEmpty()) {
            if (descriptor.isRequired()) {
                this.raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
            }
            //如果按照类型没有匹配上，则返回为空，返回大于0个则往下走
            return null;
        }
        String autowiredBeanName;
        Object instanceCandidate;
        // 存在多个匹配项
        if (matchingBeans.size() > 1) {
            // 按照@Primary, @Priority ,名称匹配 分别匹配
            autowiredBeanName = this.determineAutowireCandidate(matchingBeans, descriptor);
            if (autowiredBeanName == null) {
                if (descriptor.isRequired() || !this.indicatesMultipleBeans(type)) {
                    return descriptor.resolveNotUnique(type, matchingBeans);
                } else {
                    return null;
                }
            }
            instanceCandidate = matchingBeans.get(autowiredBeanName);
        } else {
            // 存在唯一的匹配,按照类型匹配，仅有一个实例匹配上
            Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
            autowiredBeanName = entry.getKey();
            instanceCandidate = entry.getValue();
        }

````
##### determineAutowireCandidate 基于优先级匹配来获取注入的实例
````
protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
		Class<?> requiredType = descriptor.getDependencyType();
        //@Primary 注解
		String primaryCandidate = determinePrimaryCandidate(candidates, requiredType);
		if (primaryCandidate != null) {
			return primaryCandidate;
		}
        //@Priority 注解
		String priorityCandidate = determineHighestPriorityCandidate(candidates, requiredType);
		if (priorityCandidate != null) {
			return priorityCandidate;
		}
		// Fallback
		for (Map.Entry<String, Object> entry : candidates.entrySet()) {
			String candidateName = entry.getKey();
			Object beanInstance = entry.getValue();
            //通过matchesBeanName方法来确定bean集合中的名称是否与属性的名称相同，此时也就是按照名字匹配
			if ((beanInstance != null && this.resolvableDependencies.containsValue(beanInstance)) || matchesBeanName(candidateName, descriptor.getDependencyName())) {
				return candidateName;
			}
		}
		return null;
	}
````
#### MethodOverrides

- MethodOverrides的作用就是在spring配置中存在lookup-method 和replace-method 的，而这两个配置在加载xml的时候就会统一存放在BeanDefinition中的methodOverrides属性里;

- 遍历MethodOverrides,对于一个方法的匹配来讲，如果一个类中存在若干个重载方法，那么，在函数调用以及增强的时候还需要根据参数类型进行匹配，来最终确认当前调用的到底是哪个函数，
但是，spring将一部分匹配工作在这里完成了，如果当前类中的方法只有一个，那么就设置重载该方法没有被重载，这样在后续调用的时候便可以直接使用找到的方法，而不需要进行方法的参数匹配了，而且还可以提前对方法存在性进行验证;

