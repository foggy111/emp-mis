package com.lab.emp.dto;

/**
 * 员工列表查询条件。对应 GET /emps 的 name、gender、dept。
 * 分页参数 page、size 由 Controller 单独接收，不放进实体。
 */
public class EmpQueryDTO {

    private String name;
    private String gender;
    private String dept;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
