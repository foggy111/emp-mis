package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.EmpService;
import org.springframework.stereotype.Service;

/**
 * 员工业务实现。通用新增、修改、删除、按主键查询由 ServiceImpl 提供。
 */
@Service
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements EmpService {
}
