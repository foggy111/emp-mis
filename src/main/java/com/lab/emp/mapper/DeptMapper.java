package com.lab.emp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.emp.entity.Dept;

/**
 * 部门持久层。部门与员工为一对多，员工侧外键是 emp.dept_id。
 */
public interface DeptMapper extends BaseMapper<Dept> {
}
