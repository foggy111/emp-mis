package com.lab.emp.support;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;

/**
 * 记录提交与回滚次数，用来断言事务失效场景。
 */
public class RecordingTransactionManager extends DataSourceTransactionManager {

    private static final long serialVersionUID = 1L;

    private int commits;
    private int rollbacks;

    public RecordingTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    public int getCommits() {
        return commits;
    }

    public int getRollbacks() {
        return rollbacks;
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        commits++;
        super.doCommit(status);
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        rollbacks++;
        super.doRollback(status);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
    }
}
