package com.lab.emp.entity;

import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmpValidationTest {

    private Validator validator;

    @Before
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void blankNameRejected() {
        Emp emp = valid();
        emp.setEmpName("  ");
        assertEquals("员工姓名不能为空", message(emp));
    }

    @Test
    public void negativeSalaryRejected() {
        Emp emp = valid();
        emp.setSalary(new BigDecimal("-0.01"));
        assertEquals("薪资不能为负数", message(emp));
    }

    @Test
    public void missingSalaryRejected() {
        Emp emp = valid();
        emp.setSalary(null);
        assertEquals("薪资不能为空", message(emp));
    }

    @Test
    public void oversizedNameRejected() {
        Emp emp = valid();
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            name.append('名');
        }
        emp.setEmpName(name.toString());
        assertEquals("姓名长度不能超过50", message(emp));
    }

    @Test
    public void zeroSalaryAccepted() {
        Emp emp = valid();
        emp.setSalary(BigDecimal.ZERO);
        assertTrue(validator.validate(emp).isEmpty());
    }

    private String message(Emp emp) {
        Set<ConstraintViolation<Emp>> violations = validator.validate(emp);
        assertEquals(1, violations.size());
        return violations.iterator().next().getMessage();
    }

    private Emp valid() {
        Emp emp = new Emp();
        emp.setEmpName("张伟");
        emp.setGender("男");
        emp.setDept("研发部");
        emp.setPost("Java工程师");
        emp.setSalary(new BigDecimal("12000.00"));
        emp.setStatus(Integer.valueOf(1));
        return emp;
    }
}
