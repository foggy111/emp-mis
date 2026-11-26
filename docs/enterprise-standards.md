# 企业开发规范清单（任务 1 自检表）

依据《JAVA框架技术（一）》实验指导书第三部分任务 1「双师课堂：企业级 SSM 项目实战解析」的提纲整理。提纲要求覆盖：标准开发流程与 MIS 架构、分层职责与命名、Git 协作与代码评审、AI 生成代码的边界与安全合规，以及依赖冲突、事务失效、性能瓶颈的处理方式。

本文是后续 `emp-mis` 开发的自检依据，不是课堂录音整理，也不包含虚构的出勤或发言记录。每一条都写明「规范」和「本项目怎么检查」。

适用项目：EMP 员工管理 MIS（经典 SSM，打包方式 war，基础包 `com.lab.emp`）。

## 开发流程（对照用，不单列成一条规范）

需求分析 → 架构设计 → 编码 → 测试 → 部署。本实验的落地顺序与指导书任务划分一致：规范自检、Maven 工程、全注解整合、库表与分层、RESTful 接口、健壮性与事务、ApiFox 复盘。

---

## 1. 分层架构职责边界

**规范**

- `controller` 只做协议适配：路径、参数绑定、校验结果、调用 Service、返回 `Result`。不写 SQL，不拼装查询条件。
- `service` 承载业务规则与事务边界。跨表、批量、回滚都放在这一层。
- `mapper` 只负责持久化，继承 MyBatis-Plus `BaseMapper`，不写业务判断。
- `entity` 对应表，`dto` 对应查询或入参，二者不混用。页面查询条件走 `EmpQueryDTO`，不要把分页参数塞进 `Emp`。
- `config`、`common`、`interceptor`、`aspect` 不反向依赖具体业务细节以外的层（拦截器只看会话，切面只记日志）。

**自检**

- Controller 方法体能否在不打开 Mapper 的情况下读懂。
- 根容器扫描 `com.lab.emp` 并排除 `@Controller` / `@ControllerAdvice`；Spring MVC 子容器只扫描 Controller 与需要在子容器生效的异常处理和切面。

## 2. 命名规范

**规范**

- 包名全小写：`com.lab.emp.*`，与指导书目录一致，不另起 `com.example`。
- 类名大驼峰，见名知意：`EmpController`、`EmpServiceImpl`、`GlobalExceptionHandler`、`LoginInterceptor`、`WebLogAspect`。
- 方法名动宾结构：`pageQuery`、`saveWithTx`、`deleteBatch`。
- 表名、字段名小写下划线：`emp_name`、`hire_date`。Java 属性小驼峰，靠 `mapUnderscoreToCamelCase` 映射。
- RESTful 资源用复数名词 `/emps`，不使用 `/addEmp`、`/deleteEmp` 这类动词路径。
- 数据库主键 `emp_id`，属性 `empId`，`@TableId` 类型 `IdType.AUTO`。

**自检**

- 新增类是否落在规定包内。
- 接口路径是否与实验要求逐字一致：`POST /emps`、`DELETE /emps/{id}`、`DELETE /emps`、`PUT /emps`、`GET /emps/{id}`、`GET /emps`。

## 3. 统一返回与错误语义

**规范**

- 对外 JSON 统一为 `Result<T>`：`code`、`message`、`data`。
- `200` 成功，`500` 业务失败或系统兜底，`401` 未登录。成功时 `message` 为 `success`。
- 业务可预期的失败抛 `BusinessException`（或在校验分支直接 `Result.fail`），由全局异常处理转换成 `500`。
- 未捕获异常记完整堆栈到日志，响应固定为「系统繁忙，请稍后重试」，不把堆栈、SQL、文件路径返回给调用方。
- 未登录由拦截器直接写 JSON：`{"code":401,"message":"未登录"}`。

**自检**

- 任意接口的失败响应是否仍是上述三个字段。
- 浏览器或 ApiFox 中是否看不到 Java 异常类名。

## 4. Git 提交与版本标记

**规范**

- 提交信息使用约定式前缀：`feat`、`fix`、`docs`、`chore`、`refactor`。
- 一次提交只做一类改动，按实验任务递进，后一次建立在前一次之上。
- 不提交 `target/`、IDE 工程文件、本机数据库口令、日志和系统垃圾文件。
- 里程碑使用附注标签。本实验收尾标签为 `v1.0.0`。
- 提交前先看 `git diff`，确认没有把生成物和密钥带进去。

**自检**

- `git log` 能按任务读出开发轨迹。
- `git status` 不出现 `jdbc.local.properties` 和 `target/`。

## 5. 代码评审要点

**规范**

合并或提交前至少核对下面几项：

- 空指针：`BindingResult.getFieldError()`、查询结果、请求体列表都可能为空。
- 事务注解是否加在会被 Spring 代理调用的 `public` 方法上，`rollbackFor` 是否包含需要回滚的异常。
- 是否存在自调用（`this.xxx()`）导致注解失效。
- 日志是否打印了密码、盐或完整令牌。
- 命名、分层、返回值是否符合第 1–3 条。
- 删除和更新是否处理「记录不存在」「主键缺失」。

**自检**

- 评审意见要落到具体方法和行，不写「整体看一下」。
- AI 改过的文件同样走这份清单，不因为来源是 AI 而跳过。

## 6. AI 辅助的边界

**规范**

可以交给 AI 的部分：依赖清单草稿、样板配置、Getter/Setter、测试数据、异常堆栈的初步分诊、文档结构。

必须自己完成的部分：

- 核对版本兼容矩阵（Spring 5.3.x、MyBatis-Plus 3.5.x、JDK 8/11/17、Tomcat 9、`javax.*` 而不是 `jakarta.*`）。
- 逐行阅读生成代码后再保留，尤其是事务、拦截器、异常处理和 SQL。
- 不把真实账号口令、生产连接串、个人隐私发给 AI。
- 实验报告里的 AI 记录写实际提示词、生成了什么、人工改了什么，不编造对话。
- AI 输出与指导书冲突时，以指导书的包名、类名、路径和返回结构为准；指导书示例本身有语法问题时，修正并在报告中说明。

**自检**

- 报告「AI 应用记录」能否指出至少一处人工修改。
- 仓库中搜索不到真实数据库密码。

## 7. 依赖版本与冲突

**规范**

- 版本集中在 `pom.xml` 的 `<properties>`，依赖引用属性，避免同一构件出现两个版本。
- 使用经典 SSM 构件：`spring-webmvc` / `spring-jdbc` / `spring-aspects`、`mybatis-plus`（不是 `mybatis-plus-boot-starter`）、`mysql-connector-j`、`javax.servlet-api`（`provided`）。
- Servlet 规范停留在 4.0，容器使用 Tomcat 9。不引入 Spring Boot，也不把 `jakarta.servlet` 和 `javax.servlet` 混在一个 war 里。
- Hibernate Validator 6.2 使用 `javax.validation`，并补上 EL 实现，否则校验器启动时会找 `ExpressionFactory`。
- 新增依赖前先看它传递引入了什么，用 `mvn dependency:tree` 确认没有第二套 Spring 或 MyBatis。

**自检**

- `mvn -DskipTests compile` 在 JDK 11 与 17 上也能通过（源码级别保持 8）。
- war 包里没有打进 `javax.servlet-api`（scope 为 provided）。

## 8. 事务规范

**规范**

- 事务加在 Service 的 public 方法上，使用 `@Transactional(rollbackFor = Exception.class)`。默认只回滚运行时异常，受检异常需要 `rollbackFor` 才能覆盖。
- 一个事务只包住真正要一起成功或一起失败的写操作，不把远程调用和长耗时逻辑包进来。
- 下列写法视为失效，代码评审直接打回，并在测试或说明里留记录：
  1. 异常在方法内部被 `try/catch` 吃掉，代理看不到异常，事务提交。
  2. 同类自调用 `this.xxx()`，调用不经过代理，注解不生效。
  3. `@Transactional` 加在 `private` 方法上，Spring 默认只代理 public 方法。
- 验证回滚用专门方法 `saveWithTx`：先插入再人为抛错，确认表中没有残留行。

**自检**

- 失效场景有对应测试或文档，写明「现象」和「原因」，而不是只贴注解。

## 9. 性能与 SQL

**规范**

- 列表必须分页。默认 `page=1`、`size=5`，禁止无上限地把全表装进内存。
- 条件查询走索引列。`emp_name` 建普通索引；等值条件优先用于 `dept`、`gender`。
- 只查询业务需要的列，不在循环里逐条查库（避免 N+1）。
- 分页参数要挡住非法值：`page < 1` 不能生成负偏移，`size <= 0` 不能变成全表扫描。
- 慢 SQL 通过 Log4j 打印的语句排查，优化手段是补索引、收窄条件、减少回表，而不是先加缓存。

**自检**

- `GET /emps` 的 SQL 带 `LIMIT`，且 `emp_name` 条件能用到 `idx_emp_name`（前导模糊 `%关键字` 时索引会退化，报告里要承认这一点）。

## 10. 禁止硬编码密钥与口令

**规范**

- `jdbc.properties` 只放占位密码 `your_password`。真实口令写在 `jdbc.local.properties`，该文件被 `.gitignore` 忽略，并通过 `@PropertySource(..., ignoreResourceNotFound = true)` 覆盖占位配置。
- 用户密码只存摘要。本项目使用 SHA-256（盐 + 明文）的十六进制摘要，盐随机或按账号固化在库中，不明文入库、不写进日志、不出现在 Git 历史。
- 测试账号可以公开（如 `admin` / `123456`），公开的是口令本身作为实验数据，库里仍然是摘要。
- 日志切面打印参数时去掉 `password` 字段。

**自检**

- `git grep -n "your_password"` 能找到占位符；`git grep` 找不到本机真实密码。
- `sql/init.sql` 中的 `password_hash` 不是明文 `123456`。

## 11. 日志规范

**规范**

- 使用 Log4j 1.2.x 同时输出运行日志和 MyBatis SQL（`logImpl = LOG4J`，mapper 包级别 `DEBUG`）。
- 请求日志用 AOP `@Around` 包住 Controller：方法签名、脱敏后的参数、耗时、异常消息。优先用 Logger，而不是只 `System.out`。
- `ERROR` 记堆栈，`INFO` 记关键业务结果，`DEBUG` 记 SQL。循环内不打 INFO。
- 生产环境兜底异常不再 `printStackTrace` 到标准错误，统一进日志文件或控制台日志。

**自检**

- 启动后能看到 Mapper 的 SQL；登录请求的日志里没有明文密码。

## 12. 参数校验与输入安全

**规范**

- 写接口使用 JSR-303：`empName` 非空且最长 50，`salary` 非空且不小于 0。Controller 使用 `@Valid`，并用 `BindingResult` 返回第一条字段错误。
- 另备 `MethodArgumentNotValidException` 的全局处理，避免将来有人漏写 `BindingResult` 时把框架错误页暴露出去。
- SQL 条件使用 `#{}` / Lambda 条件构造器，不拼接 `${}`。
- 批量删除的空数组要显式拒绝，避免生成非法的 `IN ()`。

**自检**

- 空姓名、负薪资、超长姓名均返回 `code=500` 和注解里的中文提示，HTTP 响应体不是 HTML 错误页。

## 13. 安全访问控制

**规范**

- 除登录、注册、预检请求和静态资源外，接口要求会话里存在 `loginUser`。
- 拦截器只负责「是否已登录」，业务权限不塞进切面。
- 注册接口同样走摘要存储，用户名重复时返回业务失败，不覆盖已有账号。
- 跨域按指导书配置：`allowedOriginPatterns("*")`、允许 GET/POST/PUT/DELETE/OPTIONS、`allowCredentials(true)`。不要改成 `allowedOrigins("*")` 与 `allowCredentials(true)` 的非法组合。

**自检**

- 不带会话访问 `GET /emps` 得到 `code=401`。
- `POST /login`、`POST /register` 在未登录时仍可访问。

## 14. 测试与可重复的数据库脚本

**规范**

- `sql/init.sql` 可重复执行：先建库，按外键顺序删除再创建，并写入指导书中的 4 条员工样例。
- 接口测试覆盖正常路径、校验失败、不存在的主键、未登录、空批量删除、分页边界（第 0 页、超过末页、`size` 为 0 或负数）。
- 不依赖数据库的测试（校验器、密码摘要、拦截器、事务代理行为）必须在没有 MySQL 时也能通过。依赖数据库的用例在连不上库时跳过，不能让构建失败。
- 测试记录写预期 JSON。暂时没有运行截图的，明确标成待补，不伪造截图。

**自检**

- `mvn test` 在无数据库环境下退出码为 0。
- ApiFox 集合里能按文件夹找到上述场景。

---

## 提交前总检（10 秒版）

| 序号 | 检查项 | 通过 |
| --- | --- | --- |
| 1 | 分层未被打穿，Controller 无 SQL | |
| 2 | 包名、类名、URL 与指导书一致 | |
| 3 | 返回值只有 200 / 500 / 401 三种 code | |
| 4 | 提交信息带 feat/fix/docs/chore，且无密钥 | |
| 5 | 空指针、事务、日志脱敏已看过 | |
| 6 | AI 生成片段已逐段改过并记入报告 | |
| 7 | 依赖版本在 properties 中，无 Boot / jakarta 混用 | |
| 8 | 事务失效三种写法有说明或测试 | |
| 9 | 列表分页，`emp_name` 有索引 | |
| 10 | 口令是摘要，jdbc 密码可被本地文件覆盖 | |
| 11 | SQL 与请求日志走 Log4j | |
| 12 | 空姓名、负薪资被拒绝 | |
| 13 | 未登录返回 401 JSON，登录注册被排除 | |
| 14 | init.sql 可重复执行，无库时测试仍通过 | |
