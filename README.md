# emp-mis

EMP 员工管理 MIS。经典 SSM（Spring 5.3.31 + Spring MVC + MyBatis-Plus 3.5.3.1），Maven 打包 `war`，不使用 Spring Boot。基础包 `com.lab.emp`。

- 学生：牛昭凯
- 学号：20241921
- 班级：软件工程6班
- 学校：天津理工大学
- 课程：《JAVA框架技术（一）》实验二

本仓库按实验任务分成 7 次提交，里程碑标签 `v1.0.0`。GitHub 用户名为 foggy111。当前没有对应的 GitHub 仓库，不要把含真实数据库口令的文件推上去。

## 环境

- JDK 8、11 或 17（源码级别 8，本机用更高版本的 JDK 也可以编译）
- Maven 3.8+
- MySQL 8.0
- Tomcat 9（不要用 Tomcat 10，Servlet 包名仍是 `javax.servlet`）
- IntelliJ IDEA，可用 Smart Tomcat 插件
- ApiFox

## 1. 创建数据库

```sql
-- 在 MySQL 客户端执行仓库里的脚本
source /你的路径/sql/init.sql
```

脚本会创建库 `ssm_emp`，写入部门、4 名样例员工，以及登录账号。可以重复执行。

样例员工：张伟、李娜、王强、赵敏。

## 2. 填写本机数据库口令

`src/main/resources/jdbc.properties` 里的密码是占位符 `your_password`，不要改成真实口令再提交。

```bash
cp jdbc.local.properties.example src/main/resources/jdbc.local.properties
```

编辑 `src/main/resources/jdbc.local.properties`，把 `jdbc.password` 改成你的 MySQL 密码。这个文件已在 `.gitignore` 中。文件不存在时，工程仍能编译；连库时才会失败。

## 3. 用 Tomcat 9 或 Smart Tomcat 运行

1. IDEA 中导入 Maven 项目，等待依赖下载完成。
2. 添加 Tomcat 9 本地服务器，部署 `emp-mis:war`（或 war exploded）。
3. **Application context 设为 `/`**。这样接口才是 `POST /emps`，而不是 `/emp-mis/emps`。
4. 启动后访问 [http://localhost:8080/](http://localhost:8080/) 应看到静态说明页。
5. 控制台应出现 Log4j。Mapper 包为 DEBUG，查询时能看到 SQL。

没有 web.xml。入口类是 `com.lab.emp.config.ServletContainersInitConfig`。

## 4. 默认账号

| 用户名 | 密码 | 说明 |
| --- | --- | --- |
| admin | 123456 | 写在 `sql/init.sql` 中的摘要，不是明文 |

```http
POST /login
Content-Type: application/json

{"username":"admin","password":"123456"}
```

成功后会话里有 `loginUser`。之后的 `/emps` 请求要带上同一个 Cookie。`POST /register` 可注册新账号，密码同样只存 SHA-256(盐 + 密码)。

未登录访问业务接口时，HTTP 状态为 401，响应体为：

```json
{"code":401,"message":"未登录"}
```

## 5. 用 ApiFox 测试

导入下面任一文件即可生成项目：

- `apifox/emp-mis.postman_collection.json`（推荐，用例已按场景拆开）
- `apifox/emp-mis.openapi.json`

导入后把集合变量 `baseUrl` 改成实际地址，上下文为 `/` 时是 `http://localhost:8080`。先执行「登录」，并让 ApiFox 保存 Cookie，再跑员工接口。预期结果写在 `apifox/测试记录.md`。本环境没有 MySQL、Tomcat 和 ApiFox，记录里的运行截图需要你在自己电脑上补。

## 6. 只编译、不连库

```bash
mvn -q -DskipTests compile
mvn test
```

`mvn test` 不依赖数据库。连不上 MySQL 时，`EmpDatabaseOptionalTest` 会跳过，构建仍然成功。连上库并执行过 `sql/init.sql` 之后，该测试会验证 `saveWithTx` 回滚，以及异常被吃掉、自调用时数据留在表里的情况。事务说明见 `docs/transaction-failure-cases.md`。

## 目录

```text
emp-mis
├── src/main/java/com/lab/emp
│   ├── config
│   ├── controller
│   ├── service / impl
│   ├── mapper
│   ├── entity
│   ├── dto
│   ├── common
│   ├── interceptor
│   └── aspect
├── src/main/resources          jdbc.properties、log4j.properties
├── src/main/webapp/WEB-INF
├── src/test/java
├── sql/init.sql
├── docs
└── apifox
```

企业开发自检清单：`docs/enterprise-standards.md`。架构和接口：`docs/architecture.md`。实验报告：`docs/lab-report.md` 与 `docs/lab-report.docx`。
