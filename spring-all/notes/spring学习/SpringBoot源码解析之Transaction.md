# SpringBoot 之 事物分析

**Spring 支持三个不同的事务注解**

1. Spring 事务注解 org.springframework.transaction.annotation.Transactional

2. JTA事务注解 javax.transaction.Transactional

3. EJB 3 事务注解 javax.ejb.TransactionAttribute

### AnnotationTransactionAttributeSource(分析事物注解最终组织成一个TransactionAttribute供随后使用)

public class AnnotationTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource

- ClassLoader classLoader = AnnotationTransactionAttributeSource.class.getClassLoader();

- jta12Present = ClassUtils.isPresent("javax.transaction.Transactional", classLoader);//根据注解类的存在性判断 JTA 1.2 事务注解是否被使用

- private final boolean publicMethodsOnly;//指出仅仅处理public方法(基于代理的AOP情况下的典型做法),还是也处理protected/private方法(使用AspectJ类织入方式下的典型做法)

- private final Set<TransactionAnnotationParser> annotationParsers;// 保存用于分析事务注解的事务注解分析器

- public AnnotationTransactionAttributeSource(boolean publicMethodsOnly);//构造函数,指定 publicMethodsOnly值

  - this.publicMethodsOnly = publicMethodsOnly;
  
  - if (jta12Present || ejb3Present);//准备用于分析事务注解的各个分析器，放到属性 annotationParsers 中Spring事务注解分析器总是会被使用 : SpringTransactionAnnotationParser
  
    - this.annotationParsers = new LinkedHashSet<>(4);
  
    - this.annotationParsers.add(new SpringTransactionAnnotationParser());
  
    - if (jta12Present)=>this.annotationParsers.add(new JtaTransactionAnnotationParser());
  
    - if (ejb3Present)=>this.annotationParsers.add(new Ejb3TransactionAnnotationParser());

  - else{}=>this.annotationParsers = Collections.singleton(new SpringTransactionAnnotationParser());
  
- public AnnotationTransactionAttributeSource(TransactionAnnotationParser annotationParser);//构造函数,指定事物解析器
   
   ```
   创建一个定制的 AnnotationTransactionAttributeSource ，使用给定的事务注解分析器(一个),publicMethodsOnly 缺省使用 true，仅针对public方法工作
   
   ```
 
  - this.publicMethodsOnly = true;//指定 publicMethodsOnly默认值是true
   
  - this.annotationParsers = Collections.singleton(annotationParser);//放在一个不可以更改的单例集合
   
- public AnnotationTransactionAttributeSource(TransactionAnnotationParser... annotationParsers);//构造函数,指定多个事物解析器 publicMethodsOnly 缺省使用 true,仅针对public方法工作
 
   - this.publicMethodsOnly = true;//指定 publicMethodsOnly默认值是true
   
   - this.annotationParsers = new LinkedHashSet<>(Arrays.asList(annotationParsers));

- public AnnotationTransactionAttributeSource(Set<TransactionAnnotationParser> annotationParsers);//构造函数,指定多个事物解析器 publicMethodsOnly 缺省使用 true，仅针对public方法工作
   
   - this.publicMethodsOnly = true;//指定 publicMethodsOnly默认值是true
   
   - this.annotationParsers = annotationParsers;
   
- protected TransactionAttribute findTransactionAttribute(Class<?> clazz);//获取某个类上的事务注解属性
  
  - return determineTransactionAttribute(clazz);//Class 实现了 AnnotatedElement 接口，所以交给下面的 determineTransactionAttribute(AnnotatedElement)执行具体的分析逻辑
  
    - TransactionAttribute attr = parser.parseTransactionAnnotation(element);
    
      ```
      分析获取某个被注解的元素，具体的来讲，指的是一个类或者一个方法上的事务注解属性。该实现会遍历自己属性annotationParsers中所包含的事务注解属性分析器试图获取事务注解属性,
      
      一旦获取到事务注解属性则返回，如果获取不到则返回null，表明目标类/方法上没有事务注解。
      
      ```

### AbstractFallbackTransactionAttributeSource
   
public abstract class AbstractFallbackTransactionAttributeSource implements TransactionAttributeSource 

- private static final TransactionAttribute NULL_TRANSACTION_ATTRIBUTE = new DefaultTransactionAttribute();//针对没有事务注解属性的方法进行事务注解属性缓存时使用的特殊值，用于标记该方法没有事务注解属性,从而不用在首次缓存在信息后,不用再次重复执行真正的分析

- public TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass);//获取指定方法上的注解事务属性如果方法上没有注解事务属性，则使用目标方法所属类上的注解事务属性

  - if (method.getDeclaringClass() == Object.class) => return null;//如果目标方法是内置类Object上的方法，总是返回null，这些方法上不应用事务
  
  - Object cacheKey = getCacheKey(method, targetClass);//先查看针对该方法是否已经获取过其注解事务属性并且已经缓存
  
  - TransactionAttribute cached = this.attributeCache.get(cacheKey);
  
    #### 目标方法上的事务注解属性信息已经缓存的情况
  
    - if (cached == NULL_TRANSACTION_ATTRIBUTE) => return null;//目标方法上上并没有事务注解属性，但是已经被尝试分析过并且已经被缓存，使用的值是 NULL_TRANSACTION_ATTRIBUTE,所以这里再次尝试获取其注解事务属性时，直接返回 null
  
    - else =>return cached;
    
  - TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);//目标方法上的注解事务属性尚未分析过，现在分析获取之(查找目标方法上的事务注解属性，但只是查找和返回，并不做缓存,效果上讲，可以认为#getTransactionAttribute 是增加了缓存机制的方法#computeTransactionAttribute)
  
    - if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers()));// 如果事务注解属性分析仅仅针对public方法，而当前方法不是public，则直接返回null
    
    - Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
    
      ```
      参数 method 可能是基于接口的方法，该接口和参数targetClass所对应的类不同(也就是说:targetClass是相应接口的某个实现类),而我们这里需要的属性是要来自targetClass的,所以这里先获取targetClass上的那个和method对应的方法,这里 method, specificMethod 都可以认为是潜在的目标方法
      
      ```
    - TransactionAttribute txAttr = findTransactionAttribute(specificMethod);//首先尝试检查事务注解属性直接标记在目标方法 specificMethod 上
    
    - if(txAttr!=null)=>return txAttr;//事务注解属性直接标记在目标方法上
    
    - txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());//然后尝试检查事务注解属性是否标记在目标方法 specificMethod 所属类上
    
    - if (txAttr != null && ClassUtils.isUserLevelMethod(method))=>return txAttr;//事务注解属性是否标记在目标方法所属类上
    
    - if (specificMethod != method);//逻辑走到这里说明目标方法specificMethod，也就是实现类上的目标方法上没有标记事务注解属性
    
      - txAttr = findTransactionAttribute(method);//如果 specificMethod 和 method 不同，则说明 specificMethod 是具体实现类的方法，method 是实现类所实现接口的方法，现在尝试从 method 上获取事务注解属性
      
      - if(txAttr!=null)=>return txAttr;
      
      - txAttr = findTransactionAttribute(method.getDeclaringClass());
      
      - if (txAttr != null && ClassUtils.isUserLevelMethod(method))=>return txAttr;
    
    - return null;//specificMethod 方法/所属类上没有事务注解属性，method 方法/所属类上也没有事务注解属性，所以返回 null

    
  - if (txAttr == null)=>this.attributeCache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);
    
  - else=> String methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
    
  - this.attributeCache.put(cacheKey, txAttr);




















		