-- EMP 员工管理 MIS 初始化脚本（实验二任务 4）
-- 沿用实验一数据库 ssm_emp 与 emp 表，并补充：
--   1. emp_name 普通索引
--   2. 非空、默认值与长度约束
--   3. 部门表 dept，与 emp.dept_id 一对多；emp.dept 仍保留，供 GET /emps?dept= 使用
--   4. 登录账号 sys_user，只保存 SHA-256 摘要
-- 本脚本可重复执行。

CREATE DATABASE IF NOT EXISTS ssm_emp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ssm_emp;

DROP TABLE IF EXISTS emp;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS dept;

CREATE TABLE dept (
    dept_id   INT         NOT NULL AUTO_INCREMENT COMMENT '部门编号',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    PRIMARY KEY (dept_id),
    UNIQUE KEY uk_dept_name (dept_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门';

CREATE TABLE emp (
    emp_id    INT            NOT NULL AUTO_INCREMENT COMMENT '员工编号',
    emp_name  VARCHAR(50)    NOT NULL COMMENT '姓名',
    gender    CHAR(1)        NOT NULL DEFAULT '男' COMMENT '性别',
    dept      VARCHAR(50)    NOT NULL DEFAULT '' COMMENT '部门名称',
    dept_id   INT            NULL COMMENT '部门外键',
    post      VARCHAR(50)    NOT NULL DEFAULT '' COMMENT '岗位',
    salary    DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '薪资',
    hire_date DATE           NULL COMMENT '入职时间',
    status    TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：1在职 0离职',
    PRIMARY KEY (emp_id),
    KEY idx_emp_name (emp_name),
    KEY idx_emp_dept (dept),
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工';

CREATE TABLE sys_user (
    user_id       INT         NOT NULL AUTO_INCREMENT COMMENT '用户编号',
    username      VARCHAR(32) NOT NULL COMMENT '登录名',
    password_hash CHAR(64)    NOT NULL COMMENT 'SHA-256(salt + password) 小写十六进制',
    salt          CHAR(16)    NOT NULL COMMENT '盐',
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录账号';

INSERT INTO dept (dept_id, dept_name) VALUES
    (1, '研发部'),
    (2, '市场部'),
    (3, '人事部');

-- 指导书实验一的 4 条样例，部门编号与 dept 表对应
INSERT INTO emp (emp_name, gender, dept, dept_id, post, salary, hire_date, status) VALUES
    ('张伟', '男', '研发部', 1, 'Java工程师', 12000.00, '2024-07-01', 1),
    ('李娜', '女', '研发部', 1, '前端工程师', 10000.00, '2024-08-15', 1),
    ('王强', '男', '市场部', 2, '市场专员',   8000.00, '2023-03-10', 1),
    ('赵敏', '女', '人事部', 3, '人事专员',   7500.00, '2022-06-20', 0);

-- 测试账号 admin / 123456。库中不是明文。
-- 摘要算法：SHA-256( UTF-8(salt + password) )，结果为小写十六进制。
-- salt = a1b2c3d4e5f60718
-- hash = 85cc99c527a2588b8d387b5a7f75008ac9561738dc9e18d1b9e74699e1718bbf
INSERT INTO sys_user (username, password_hash, salt) VALUES
    ('admin', '85cc99c527a2588b8d387b5a7f75008ac9561738dc9e18d1b9e74699e1718bbf', 'a1b2c3d4e5f60718');
