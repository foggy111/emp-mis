package com.lab.emp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String empName;

    @Size(max = 1, message = "性别长度不能超过1")
    private String gender;

    @Size(max = 50, message = "部门长度不能超过50")
    private String dept;

    private Integer deptId;

    @Size(max = 50, message = "岗位长度不能超过50")
    private String post;

    @NotNull(message = "薪资不能为空")
    @DecimalMin(value = "0", message = "薪资不能为负数")
    private BigDecimal salary;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
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
