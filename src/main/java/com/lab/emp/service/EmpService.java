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

    /**
     * 插入后人为抛出算术异常。方法上的事务应回滚插入。
     */
    void saveWithTx(Emp emp);

    /**
     * 同样插入后抛错，但异常被方法内部吃掉，事务会提交。
     */
    void saveWithSwallowedException(Emp emp);

    /**
     * 通过 this 调用 {@link #saveWithTx(Emp)}，不经过代理，事务注解不生效。
     */
    void saveBySelfInvocation(Emp emp);

    /**
     * 调用带 {@code @Transactional} 的 private 方法。Spring 不代理 private 方法。
     */
    void callPrivateSave(Emp emp);
}
