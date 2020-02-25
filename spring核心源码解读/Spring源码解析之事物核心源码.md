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

  - return handleExistingTransaction(definition, transaction, debugEnabled);//对于已经存在的事物，根据不同传播机制不同处理
    
    - if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER);//1.NERVER（不支持当前事务;如果当前事务存在，抛出异常）报错
    
      - throw new IllegalTransactionStateException("Existing transaction found for transaction marked with propagation 'never'");
    
    - if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED);//2.NOT_SUPPORTED（不支持当前事务，现有同步将被挂起）挂起当前事务
      
      - Object suspendedResources = suspend(transaction);//挂起当前事务
      
        ```
        suspendedResources 所谓挂起事物，就是把目前线程中所有储存的信息，都保存起来，返回一个suspendedResources对象并且把当前线程中的事物相关信息都清空，方便下一个事物newSynchronization和prepareTransactionStatus（）中判断
        来更新到线程中
        
        ```
      - boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);//创建一个空事务
 
      - return prepareTransactionStatus(definition, null, false, newSynchronization, debugEnabled, suspendedResources);//Create a new TransactionStatus for the given arguments,also initializing transaction synchronization as appropriate.

    - if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW);//3. REQUIRES_NEW挂起当前事务，创建新事务
      
      - SuspendedResourcesHolder suspendedResources = suspend(transaction);//挂起当前事务
      
      - boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
      
      - DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
     
      - doBegin(transaction, definition);
      
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

### rollback






