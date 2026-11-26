package com.lab.emp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Emp;

/**
 * 员工业务接口。
 */
public interface EmpService extends IService<Emp> {

    /**
     * 分页 + 多条件。姓名模糊，性别与部门等值。
     * page 小于 1 时按第 1 页处理；size 小于 1 时不查数据行，只返回总数。
     */
    Page<Emp> pageQuery(int page, int size, EmpQueryDTO query);
}
