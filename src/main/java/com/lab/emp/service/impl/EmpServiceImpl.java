package com.lab.emp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Emp;
import com.lab.emp.mapper.EmpMapper;
import com.lab.emp.service.EmpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 员工业务实现。通用新增、修改、删除、按主键查询由 ServiceImpl 提供。
 */
@Service
public class EmpServiceImpl extends ServiceImpl<EmpMapper, Emp> implements EmpService {

    @Override
    public Page<Emp> pageQuery(int page, int size, EmpQueryDTO query) {
        EmpQueryDTO criteria = query == null ? new EmpQueryDTO() : query;
        int current = page < 1 ? 1 : page;
        LambdaQueryWrapper<Emp> wrapper = new LambdaQueryWrapper<Emp>();
        wrapper.like(StringUtils.isNotBlank(criteria.getName()), Emp::getEmpName, criteria.getName())
                .eq(StringUtils.isNotBlank(criteria.getGender()), Emp::getGender, criteria.getGender())
                .eq(StringUtils.isNotBlank(criteria.getDept()), Emp::getDept, criteria.getDept())
                .orderByDesc(Emp::getEmpId);
        if (size < 1) {
            Page<Emp> empty = new Page<Emp>(current, 0);
            empty.setRecords(new ArrayList<Emp>(Collections.<Emp>emptyList()));
            Long total = baseMapper.selectCount(wrapper);
            empty.setTotal(total == null ? 0L : total);
            return empty;
        }
        return baseMapper.selectPage(new Page<Emp>(current, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithTx(Emp emp) {
        baseMapper.insert(emp);
        triggerArithmeticException();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithSwallowedException(Emp emp) {
        try {
            baseMapper.insert(emp);
            triggerArithmeticException();
        } catch (ArithmeticException ex) {
            // 异常没有抛出代理，事务管理器会提交而不是回滚
        }
    }

    @Override
    public void saveBySelfInvocation(Emp emp) {
        this.saveWithTx(emp);
    }

    @Override
    public void callPrivateSave(Emp emp) {
        privateSave(emp);
    }

    @Transactional(rollbackFor = Exception.class)
    private void privateSave(Emp emp) {
        baseMapper.insert(emp);
        triggerArithmeticException();
    }

    private static void triggerArithmeticException() {
        int value = 1 / 0;
        if (value == 0) {
            return;
        }
    }
}
