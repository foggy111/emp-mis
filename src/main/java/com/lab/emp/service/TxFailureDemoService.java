package com.lab.emp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 不访问数据库，只观察事务代理是否生效。
 * 员工表上的对应方法见 {@link EmpService}。
 */
@Service
public class TxFailureDemoService {

    @Transactional(rollbackFor = Exception.class)
    public void rollbackOnException() {
        throw new IllegalStateException("预期回滚");
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean swallowedDoesNotMarkRollback() {
        try {
            throw new IllegalStateException("被吃掉");
        } catch (IllegalStateException ex) {
            return TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean transactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    public boolean selfInvocationSeesTransaction() {
        return this.transactionActive();
    }

    public boolean privateAnnotationSeesTransaction() {
        return privateTransactional();
    }

    @Transactional(rollbackFor = Exception.class)
    private boolean privateTransactional() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
}
