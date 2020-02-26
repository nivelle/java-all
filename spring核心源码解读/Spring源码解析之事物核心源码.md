# SpringBoot 之 事物核心源码

```
public interface PlatformTransactionManager {
    // 获取事务状态
    TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;
　　// 事务提交
    void commit(TransactionStatus status) throws TransactionException;
　　// 事务回滚
    void rollback(TransactionStatus status) throws TransactionException;
}
```
### getTransaction

public final TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException

- TransactionDefinition def = (definition != null ? definition : TransactionDefinition.withDefaults());//		// Use defaults if no transaction definition given.

- Object transaction = doGetTransaction();//抽象方法，具体的实现由具体的事务处理器提供

- if (isExistingTransaction(transaction)) ;//检查当前线程是否存在事务

  - return **handleExistingTransaction(definition, transaction, debugEnabled);**//对于已经存在的事物，根据不同传播机制不同处理
    
    - if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER);//1.NERVER（不支持当前事务;如果当前事务存在，抛出异常）报错
    
      - throw new IllegalTransactionStateException("Existing transaction found for transaction marked with propagation 'never'");
    
    - if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED);//2.NOT_SUPPORTED（不支持当前事务，现有同步将被挂起）挂起当前事务
      
      - Object suspendedResources = suspend(transaction);//挂起当前事务
      
        ```
        suspendedResources 所谓挂起事物，就是把目前线程中所有储存的信息，都保存起来，返回一个suspendedResources对象并且把当前线程中的事物相关信息都清空，方便下一个事物newSynchronization和prepareTransactionStatus（）中判断
        来更新到线程中
        
        ```
        - **protected final SuspendedResourcesHolder suspend(Object transaction) throws TransactionException** 
          
          - if (TransactionSynchronizationManager.isSynchronizationActive())//**1.当前存在同步**
          
          - List<TransactionSynchronization> suspendedSynchronizations = doSuspendSynchronization();//执行注册方法，并全部取出，把当前线程事物设置为不同步状态，说明这个事物已经被挂起了
          
          - suspendedResources = doSuspend(transaction);//**在DataSource的状态下，是取出连接的持有者对象**

          ```
          String name = TransactionSynchronizationManager.getCurrentTransactionName();
          TransactionSynchronizationManager.setCurrentTransactionName(null);
          boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
          TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
          Integer isolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
          TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(null);
          boolean wasActive = TransactionSynchronizationManager.isActualTransactionActive();
          TransactionSynchronizationManager.setActualTransactionActive(false);
          
          return new SuspendedResourcesHolder(suspendedResources, suspendedSynchronizations, name, readOnly, isolationLevel, wasActive);
          
          ```
          - catch(RuntimeException ex)
          
            - doResumeSynchronization(suspendedSynchronizations);//doSuspend failed - original transaction is still active...
           
          - (Error err)
            
            - doResumeSynchronization(suspendedSynchronizations);//doSuspend failed - original transaction is still active...
          
          - else if (transaction != null)//**2.没有同步但，事务不为空，挂起事务** 
          
            - Object suspendedResources = doSuspend(transaction);// Transaction active but no synchronization active.
               
              ##### 子类实现:DataSourceTransactionManager
              
              - **protected Object doSuspend(Object transaction)**
              
                - DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
                
                - txObject.setConnectionHolder(null);//1.**把当前事务的connectionHolder数据库连接持有者清空。**
                
                - return TransactionSynchronizationManager.unbindResource(this.dataSource);//2.**当前线程解绑datasource**.其实就是ThreadLocal移除对应变量（TransactionSynchronizationManager类中定义的private static final ThreadLocal<Map<Object, Object>> resources = new NamedThreadLocal<Map<Object, Object>>("Transactional resources");）
                                                                                                        
            - return new SuspendedResourcesHolder(suspendedResources);
          
          - else
            
            - return null;// Neither transaction nor synchronization active.

      - boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);//创建一个空事务
 
      - return prepareTransactionStatus(definition, null, false, newSynchronization, debugEnabled, suspendedResources);//Create a new TransactionStatus for the given arguments,also initializing transaction synchronization as appropriate.

    - if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW);//3. REQUIRES_NEW挂起当前事务，创建新事务
      
      - SuspendedResourcesHolder suspendedResources = suspend(transaction);//挂起当前事务
      
      - boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
      
      - DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
     
      - doBegin(transaction, definition);
      
        ##### 子类实现:DataSourceTransactionManager
        
        ```
        1.DataSourceTransactionObject“数据源事务对象”，设置ConnectionHolder，再给ConnectionHolder设置各种属性：自动提交、超时、事务开启、隔离级别。
        
        2.给当前线程绑定一个线程本地变量，key=DataSource数据源  v=ConnectionHolder数据库连接。
        
        ```
        
        - DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        
        - Connection con = null;
        
        - if (!txObject.hasConnectionHolder() || txObject.getConnectionHolder().isSynchronizedWithTransaction());//如果事务还没有connection或者connection在事务同步状态，重置新的connectionHolder
        
        - Connection newCon = this.dataSource.getConnection();
        
        - txObject.setConnectionHolder(new ConnectionHolder(newCon), true);// 重置新的connectionHolder
        
        - txObject.getConnectionHolder().setSynchronizedWithTransaction(true);//设置新的连接为事务同步中
        
        - con = txObject.getConnectionHolder().getConnection();
        
        - Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
        
        - txObject.setPreviousIsolationLevel(previousIsolationLevel);//DataSourceTransactionObject设置事务隔离级别
        
        - if (con.getAutoCommit());//**如果是自动提交切换到手动提交**
        
          - txObject.setMustRestoreAutoCommit(true);
          
          - con.setAutoCommit(false);
          
        - prepareTransactionalConnection(con, definition);// 如果只读，执行sql设置事务只读
        
        - txObject.getConnectionHolder().setTransactionActive(true);// 设置connection持有者的事务开启状态
        
        - int timeout = determineTimeout(definition);
        
        -  txObject.getConnectionHolder().setTimeoutInSeconds(timeout);// 设置超时秒数
        
        - if (txObject.isNewConnectionHolder())
        
        - TransactionSynchronizationManager.bindResource(getDataSource(), txObject.getConnectionHolder());// 绑定connection持有者到当前线程
        
      - prepareSynchronization(status, definition);
      
      - return status;
      
    - if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED);//4.NESTED嵌套事务
    
      - if (useSavepointForNestedTransaction());//是否支持保存点：非JTA事务走这个分支。AbstractPlatformTransactionManager默认是true，JtaTransactionManager复写了该方法false，DataSourceTransactionmanager没有复写，还是true
        
        - DefaultTransactionStatus status = prepareTransactionStatus(definition, transaction, false, false, debugEnabled, null);
        
        - status.createAndHoldSavepoint();// 创建保存点
        
        - return status;
        
    - else//JTA事务走这个分支，创建新事务
    
      - boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
      
      - DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, newSynchronization, debugEnabled, null);
      
      - doBegin(transaction, definition);
      
      - prepareSynchronization(status, definition);
      
      - return status;
      
- if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) //超时不能小于默认值

  - throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());

- if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY)
  
  ```
  1. 如果事务传播特性配置的是mandatory，当前没有事务存在，抛出异常.MANDATORY 是必须要有一个事物，到这里说明，上面没有已经存在的事物
  
  2. PROPAGATION_MANDATORY 如果已经存在一个事务，支持当前事务。如果没有一个活动的事务，则抛出异常
  
  ```
  - throw new IllegalTransactionStateException("No existing transaction found for transaction marked with propagation 'mandatory'");

- else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
  				definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
  				definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED)
  
   ```
   1. required 如果有事物就支持当前事物，没有就自己开启一个,PROPAGATION_REQUIRED
   
   2. required_new 如果当前无事务则开启一个事务,否则挂起当前事务并开启新事务,PROPAGATION_REQUIRES_NEW
   
   3. nested 创建一个嵌套事务，如果当前无事务则创建一个事务
   
   ```
   
   - SuspendedResourcesHolder suspendedResources = suspend(null);//挂起操作，触发相关的挂起注册的事件，把当前线程事物的所有属性都封装好，放到一个SuspendedResourcesHolder，在清空一下当前线程事物，返回SuspendedResourcesHolder
   
   - boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
     
     ```
     1. 不激活和当前线程绑定的事务，因为事务传播特性配置要求创建新的事务;
     
     2. newSynchronization 在 prepareSynchronization 中会通过这个字段来决定是否把事物更新到当前线程中
     
     3. 在 newTransactionStatus()创建新事物过程中，会判断当前线程是否绑定事物,如果绑定就不可以赋值覆盖
     
     ```
   - DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);//创建一个新的事务状态  

   - doBegin(transaction, definition);//创建事务的调用，具体实现由具体的事务处理器提供，例如:datasource事物管理 

   - prepareSynchronization(status, definition);//预备同步事务状态  

- else //没有事物的情况当前不存在事务，且传播机制=PROPAGATION_SUPPORTS/PROPAGATION_NOT_SUPPORTED/PROPAGATION_NEVER这三种情况，创建空事物:没有实际事物，但是可能同步。
   
   **定义了隔离级别，但并没有真实的事务初始化，隔离级别被忽略**

  - boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
   
  - return prepareTransactionStatus(definition, null, true, newSynchronization, debugEnabled, null);
      
### commit

- public final void commit(TransactionStatus status) throws TransactionException

  - if (status.isCompleted());//如果事务已经完结，报错无法再次提交
  
    - throw new IllegalTransactionStateException("Transaction is already completed - do not call commit or rollback more than once per transaction");  
  
  - DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
  
    - if (defStatus.isLocalRollbackOnly()) //如果事务明确标记为回滚
    
      - processRollback(defStatus);//执行回滚
      
  - if (!shouldCommitOnGlobalRollbackOnly() && defStatus.isGlobalRollbackOnly());//如果不需要全局回滚时提交 且 全局回滚
  
    - processRollback(defStatus);//执行回滚
    
    - if (status.isNewTransaction() || isFailEarlyOnGlobalRollbackOnly());//// 仅在最外层事务边界（新事务）或显式地请求时抛出“未期望的回滚异常”
    
      -  throw new UnexpectedRollbackException("Transaction rolled back because it has been marked as rollback-only");

  - **processCommit(defStatus);**
  
    - 
  
### rollback






