package com.lab.emp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.emp.common.Result;
import com.lab.emp.dto.EmpQueryDTO;
import com.lab.emp.entity.Emp;
import com.lab.emp.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 员工管理接口。路径与实验要求一致。
 */
@RestController
@RequestMapping("/emps")
public class EmpController {

    @Autowired
    private EmpService empService;

    /** 新增员工。主键由数据库自增，忽略调用方传入的 empId。 */
    @PostMapping
    public Result<Void> add(@RequestBody Emp emp) {
        emp.setEmpId(null);
        empService.save(emp);
        return Result.ok(null);
    }

    /** 修改员工。 */
    @PutMapping
    public Result<Void> update(@RequestBody Emp emp) {
        if (emp.getEmpId() == null) {
            return Result.fail("员工编号不能为空");
        }
        if (empService.getById(emp.getEmpId()) == null) {
            return Result.fail("员工不存在");
        }
        empService.updateById(emp);
        return Result.ok(null);
    }

    /** 删除一名员工。 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        if (empService.getById(id) == null) {
            return Result.fail("员工不存在");
        }
        empService.removeById(id);
        return Result.ok(null);
    }

    /** 批量删除。请求体为员工编号数组。 */
    @DeleteMapping
    public Result<Void> deleteBatch(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("待删除的员工编号不能为空");
        }
        empService.removeByIds(ids);
        return Result.ok(null);
    }

    /** 按编号查询。 */
    @GetMapping("/{id}")
    public Result<Emp> getById(@PathVariable Integer id) {
        Emp emp = empService.getById(id);
        if (emp == null) {
            return Result.fail("员工不存在");
        }
        return Result.ok(emp);
    }

    /** 分页 + 多条件。默认第 1 页、每页 5 条。 */
    @GetMapping
    public Result<Page<Emp>> page(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  EmpQueryDTO query) {
        return Result.ok(empService.pageQuery(page, size, query));
    }
}
