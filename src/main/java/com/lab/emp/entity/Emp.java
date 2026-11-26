package com.lab.emp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工。字段与 ssm_emp.emp 对应。
 * dept 保留部门名称，供多条件查询；deptId 指向 dept 表，表示一对多中的“多”方。
 */
@TableName("emp")
public class Emp implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "emp_id", type = IdType.AUTO)
    private Integer empId;

    private String empName;

    private String gender;

    private String dept;

    private Integer deptId;

    private String post;

    private BigDecimal salary;

    private Date hireDate;

    private Integer status;

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Emp{empId=" + empId
                + ", empName='" + empName + '\''
                + ", gender='" + gender + '\''
                + ", dept='" + dept + '\''
                + ", deptId=" + deptId
                + ", post='" + post + '\''
                + ", salary=" + salary
                + ", hireDate=" + hireDate
                + ", status=" + status
                + '}';
    }
}
