# SpringBoot源码解析之Transaction事物代理

## TransactionAspectSupport

public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean


#### protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,final InvocationCallback invocation) throws Throwable;//将目标方法调用包围在事务处理逻辑中

  - TransactionAttributeSource tas = getTransactionAttributeSource();// 如果transaction attribute为空,该方法就是非事务（非编程式事务）
  
  - final TransactionAttribute txAttr = (tas != null? tas.getTransactionAttribute(method, targetClass) : null);//获取目标方法上的事务属性 
  
  - final PlatformTransactionManager tm = determineTransactionManager(txAttr);// 确定要使用的事务管理器
  
  - final String joinpointIdentification = methodIdentification(method, targetClass, txAttr);
  
  - if (txAttr == null || !(tm instanceof CallbackPreferringPlatformTransactionManager));//该 if 分支针对 tm 类型不是 CallbackPreferringPlatformTransactionManager 的情况(标准声明式事务：如果事务属性为空 或者 非回调偏向的事务管理器)
  
    - TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, joinpointIdentification);
    
    - retVal = invocation.proceedWithInvocation();//执行目标方法,这里就是一个环绕增强，在这个proceed前后可以自己定义增强实现
    
    - completeTransactionAfterThrowing(txInfo, ex);//异常时事务机制的处理:1. 该异常需要回滚事务，则回滚事务 2. 该异常无需回滚事务，则提交事务
    
    - cleanupTransactionInfo(txInfo);//finally  无论正常还是异常都会发生的事务机制的清场工作,比如当前方法的执行需要一个全新的事务，所以该方法执行事前会挂起之前可能存在的事务,现在方法执行完了，需要恢复之前的事务
      
      ```
      Reset the TransactionInfo ThreadLocal. =>transactionInfoHolder.set(this.oldTransactionInfo);

      ```
    - commitTransactionAfterReturning(txInfo);//目标方法正常执行时提交事务

  - else //该 else 分支针对 tm 类型是 CallbackPreferringPlatformTransactionManager 的情况;// 编程式事务:(回调偏向)
  
    - Object result = ((CallbackPreferringPlatformTransactionManager) tm).execute(txAttr, status -> {TransactionInfo txInfo = prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
    
    - cleanupTransactionInfo(txInfo);//finally 无论正常还是异常都会发生的事务机制的清场工作,比如当前方法的执行需要一个全新的事务，所以该方法执行事前会挂起之前可能存在的事务,现在方法执行完了，需要恢复之前的事务

#### protected PlatformTransactionManager determineTransactionManager(@Nullable TransactionAttribute txAttr);//根据指定的事务属性 txAttr 判断要使用的事务管理器 PlatformTransactionManager

  - if (txAttr == null || this.beanFactory == null) => return getTransactionManager();// 获取属性 this.transactionManager 作为缺省事务管理器 
  
  - String qualifier = txAttr.getQualifier();//结合考虑事务属性中的 qualifier 确定要使用的事务管理器,可能使用 :1. qualifier 指定的事务管理器 2. 使用 this.transactionManagerBeanName 指定的事务管理器 3. 使用 容器中唯一存在的类型为 PlatformTransactionManager 的 bean作为缺省事务管理器使用和缓存
  
  - return determineQualifiedTransactionManager(this.beanFactory, qualifier); 或者 return determineQualifiedTransactionManager(this.beanFactory, this.transactionManagerBeanName);
    
    - txManager = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, TransactionManager.class, qualifier);//根据类型获取指定的TransactionManager
  
  - PlatformTransactionManager defaultTransactionManager = getTransactionManager(); // 获取缺省事务管理器以及相应的缓存机制  
  
    - defaultTransactionManager = this.transactionManagerCache.get(DEFAULT_TRANSACTION_MANAGER_KEY);
    
      - defaultTransactionManager = this.beanFactory.getBean(TransactionManager.class);
      
      - this.transactionManagerCache.putIfAbsent(DEFAULT_TRANSACTION_MANAGER_KEY, defaultTransactionManager);


#### protected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,@Nullable TransactionAttribute txAttr, final String joinpointIdentification)

  - if (txAttr != null && txAttr.getName() == null);// 如果还没有定义名字，把连接点的ID定义成事务的名称
  
    - TransactionAttribute txAttr = new DelegatingTransactionAttribute(txAttr)
      
      - return joinpointIdentification;
      
  - TransactionStatus status= tm.getTransaction(txAttr);//根据事务属性获取事务TransactionStatus，大道归一，都是调用PlatformTransactionManager.getTransaction()
  
  - return prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
  
#### protected TransactionInfo prepareTransactionInfo(@Nullable PlatformTransactionManager tm,@Nullable TransactionAttribute txAttr, String joinpointIdentification,@Nullable TransactionStatus status)

   **构造一个TransactionInfo事务信息对象，绑定当前线程：ThreadLocal<TransactionInfo>**
    
  - TransactionInfo txInfo = new TransactionInfo(tm, txAttr, joinpointIdentification);
  
  - if (txAttr != null) =》txInfo.newTransactionStatus(status);
  
  - txInfo.bindToThread();//使用ThreadLocal为线程设置其事物信息

#### protected void commitTransactionAfterReturning(@Nullable TransactionInfo txInfo)

 - txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
 
   #### 抽象子类实现: AbstractPlatformTransactionManager
   
   - public final void commit(TransactionStatus status) throws TransactionException
   
    - if (status.isCompleted());//事物状态已经完成,抛出异常
    
    - DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
    
    - processRollback(defStatus, false) => return;
    
    - if (!shouldCommitOnGlobalRollbackOnly() && defStatus.isGlobalRollbackOnly())
    
    - processRollback(defStatus, true) => return;//回滚事物,然后具体的中间件 doRollback(DefaultTransactionStatus status)
    
    - processCommit(defStatus)=>return;//提交事物，然后具体中间件来doCommit(DefaultTransactionStatus status)