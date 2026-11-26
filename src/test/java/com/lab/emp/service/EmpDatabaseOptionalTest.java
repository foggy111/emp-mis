package com.lab.emp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.emp.config.SpringConfig;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import org.junit.Assume;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * 有 MySQL 且已执行 sql/init.sql 时验证 saveWithTx 的回滚与失效场景。
 * 连不上库时跳过，不让构建失败。
 */
public class EmpDatabaseOptionalTest {

    @Test
    public void saveWithTxRollsBackAndFailureCasesLeaveRows() {
        Properties jdbc = loadJdbc();
        assumeDatabase(jdbc);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        try {
            EmpService service = context.getBean(EmpService.class);
            EmpMapper mapper = context.getBean(EmpMapper.class);

            String rollbackName = "tx-rollback-" + System.nanoTime();
            Long before = mapper.selectCount(null);
            try {
                service.saveWithTx(employee(rollbackName));
                fail("saveWithTx 应该抛出算术异常");
            } catch (ArithmeticException expected) {
                // 事务回滚
            }
            assertEquals(before, mapper.selectCount(null));
            assertEquals(Long.valueOf(0L), mapper.selectCount(new LambdaQueryWrapper<Emp>().eq(Emp::getEmpName, rollbackName)));

            String swallowed = "tx-swallow-" + System.nanoTime();
            service.saveWithSwallowedException(employee(swallowed));
            Emp swallowedRow = mapper.selectOne(new LambdaQueryWrapper<Emp>().eq(Emp::getEmpName, swallowed));
            assertNotNull(swallowedRow);
            mapper.deleteById(swallowedRow.getEmpId());

            String selfName = "tx-self-" + System.nanoTime();
            try {
                service.saveBySelfInvocation(employee(selfName));
                fail("自调用仍会执行 1/0");
            } catch (ArithmeticException expected) {
                // 插入已经自动提交
            }
            Emp selfRow = mapper.selectOne(new LambdaQueryWrapper<Emp>().eq(Emp::getEmpName, selfName));
            assertNotNull(selfRow);
            mapper.deleteById(selfRow.getEmpId());
        } finally {
            context.close();
        }
    }

    private Emp employee(String name) {
        Emp emp = new Emp();
        emp.setEmpName(name);
        emp.setGender("男");
        emp.setDept("研发部");
        emp.setDeptId(Integer.valueOf(1));
        emp.setPost("测试");
        emp.setSalary(new BigDecimal("1.00"));
        emp.setStatus(Integer.valueOf(1));
        return emp;
    }

    private Properties loadJdbc() {
        Properties properties = new Properties();
        InputStream main = getClass().getClassLoader().getResourceAsStream("jdbc.properties");
        Assume.assumeTrue("缺少 jdbc.properties", main != null);
        try {
            properties.load(main);
            main.close();
            InputStream local = getClass().getClassLoader().getResourceAsStream("jdbc.local.properties");
            if (local != null) {
                properties.load(local);
                local.close();
            }
        } catch (Exception exception) {
            Assume.assumeTrue("无法读取 JDBC 配置: " + exception.getMessage(), false);
        }
        return properties;
    }

    private void assumeDatabase(Properties properties) {
        try {
            Class.forName(properties.getProperty("jdbc.driver"));
            DriverManager.setLoginTimeout(2);
            Connection connection = DriverManager.getConnection(
                    properties.getProperty("jdbc.url"),
                    properties.getProperty("jdbc.username"),
                    properties.getProperty("jdbc.password"));
            connection.close();
        } catch (Exception exception) {
            Assume.assumeTrue("数据库不可用，跳过集成测试: " + exception.getClass().getSimpleName(), false);
        }
    }
}
