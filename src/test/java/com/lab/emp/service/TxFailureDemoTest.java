package com.lab.emp.service;

import com.lab.emp.support.NoopDataSource;
import com.lab.emp.support.RecordingTransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TxFailureDemoTest {

    private AnnotationConfigApplicationContext context;
    private TxFailureDemoService service;
    private RecordingTransactionManager transactionManager;

    @Before
    public void setUp() {
        context = new AnnotationConfigApplicationContext(ProbeConfig.class);
        service = context.getBean(TxFailureDemoService.class);
        transactionManager = context.getBean(RecordingTransactionManager.class);
    }

    @After
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void exceptionLeavingTheProxyRollsBack() {
        try {
            service.rollbackOnException();
            fail("应当抛出异常");
        } catch (IllegalStateException expected) {
            assertEquals("预期回滚", expected.getMessage());
        }
        assertEquals(1, transactionManager.getRollbacks());
        assertEquals(0, transactionManager.getCommits());
    }

    @Test
    public void swallowedExceptionCommits() {
        assertFalse(service.swallowedDoesNotMarkRollback());
        assertEquals(1, transactionManager.getCommits());
        assertEquals(0, transactionManager.getRollbacks());
    }

    @Test
    public void directCallSeesTransaction() {
        assertTrue(service.transactionActive());
    }

    @Test
    public void selfInvocationBypassesProxy() {
        assertFalse(service.selfInvocationSeesTransaction());
        assertEquals(0, transactionManager.getCommits());
        assertEquals(0, transactionManager.getRollbacks());
    }

    @Test
    public void privateTransactionalMethodIsNotProxied() {
        assertFalse(service.privateAnnotationSeesTransaction());
        assertEquals(0, transactionManager.getCommits());
        assertEquals(0, transactionManager.getRollbacks());
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class ProbeConfig {

        @Bean
        public DataSource dataSource() {
            return new NoopDataSource();
        }

        @Bean
        public DataSourceTransactionManager transactionManager(DataSource dataSource) {
            return new RecordingTransactionManager(dataSource);
        }

        @Bean
        public TxFailureDemoService txFailureDemoService() {
            return new TxFailureDemoService();
        }
    }
}
