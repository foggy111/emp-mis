# 声明式事务：生效与三种失效

事务注解加在 `EmpServiceImpl` 的 public 方法上，并设置 `rollbackFor = Exception.class`。代理由根容器的 `@EnableTransactionManagement(proxyTargetClass = true)` 创建。

下面的现象用两类测试核对：

- `TxFailureDemoTest` 不连接 MySQL。它用一个空数据源观察代理有没有真正开启、提交或回滚事务。
- `EmpDatabaseOptionalTest` 只有本机能连上 `ssm_emp` 时才执行。连不上就跳过，构建仍然成功。

## 1. 正常回滚：`saveWithTx`

```java
@Transactional(rollbackFor = Exception.class)
public void saveWithTx(Emp emp) {
    baseMapper.insert(emp);
    // 1 / 0
}
```

调用经过 Spring 代理。插入和除零在同一个事务里。算术异常抛出代理后，事务管理器回滚，表中没有这行。

`TxFailureDemoTest.exceptionLeavingTheProxyRollsBack` 的预期：回滚 1 次，提交 0 次。

## 2. 异常被吃掉：`saveWithSwallowedException`

方法同样带 `@Transactional`，但 `try/catch` 把算术异常留在方法内部。代理看到的是正常返回，不会标记回滚，事务提交。员工行会留下来。

`TxFailureDemoTest.swallowedExceptionCommits` 的预期：方法返回时 `isRollbackOnly()` 为 false，提交 1 次，回滚 0 次。

## 3. 自调用：`saveBySelfInvocation`

```java
public void saveBySelfInvocation(Emp emp) {
    this.saveWithTx(emp);
}
```

`this.saveWithTx` 走的是目标对象，不是代理。`saveWithTx` 上的注解不会开启事务。插入按自动提交落地，随后异常抛出，但没有事务可回滚，行仍然在。

`TxFailureDemoTest.selfInvocationSeesTransaction` 的预期：自调用看到的“当前有事务”为 false，提交和回滚都是 0。

## 4. 注解在 private 方法上：`callPrivateSave` / `privateSave`

Spring 的事务属性源默认只处理 public 方法。private 方法上的 `@Transactional` 不会创建代理拦截。`callPrivateSave` 直接调用 `privateSave`，效果和自调用相同：没有事务，插入会留下。

`TxFailureDemoTest.privateTransactionalMethodIsNotProxied` 的预期：当前没有事务，提交和回滚都是 0。

## 和请求日志切面的关系

`WebLogAspect` 要包住 Controller，所以注册在 Spring MVC 子容器。父容器里的切面代理不到子容器的 Bean。事务切面代理的是 Service，两边互不替代。
