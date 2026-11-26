# 《JAVA框架技术（一）》实验报告

实验名称：AI 赋能 SSM 框架整合与 EMP 员工管理 MIS 系统开发

姓名：牛昭凯　　学号：20241921

班级：软件工程6班　　日期：2026-11-26　　指导教师：（待填写）

学院：天津理工大学 华信软件学院

实验学时：10（含双师课堂 2 学时）　　实验类型：综合性 + 设计性

## 一、实验环境

| 软件 | 版本 | 用途 |
| --- | --- | --- |
| JDK | 源码级别 8，编译时使用的运行环境可到 11/17/21 | 编译与运行 |
| IntelliJ IDEA | 2024 及以上 | 工程与 Smart Tomcat |
| Maven | 3.8+，依赖版本写在 pom 的 properties 中 | 构建 war |
| MySQL | 8.0 | 库 ssm_emp |
| Tomcat | 9.x | 运行 war，不用 Tomcat 10 |
| Spring | 5.3.31（webmvc、jdbc、aspects） | IoC、MVC、事务、AOP |
| MyBatis-Plus | 3.5.3.1，构件 mybatis-plus，不是 Boot Starter | ORM 与分页 |
| Druid | 1.2.20 | 连接池 |
| mysql-connector-j | 8.0.33 | 驱动 |
| Jackson | 2.15.3 | JSON |
| Hibernate Validator | 6.2.5.Final，javax.validation | JSR-303 |
| javax.servlet-api | 4.0.1，provided | Servlet 4 |
| JUnit | 4.13.2 | 单元测试 |
| Log4j | 1.2.17 | SQL 与运行日志 |
| Git | 本地仓库，标签 v1.0.0 | 版本 |
| ApiFox | 最新版 | 接口用例 |

另外补了三件指导书清单里没有写、但缺了跑不起来的依赖：`aspectjweaver` 1.9.7、`org.glassfish:javax.el` 3.0.0（校验器要 ExpressionFactory）、`commons-lang3` 3.12.0（示例里的 `StringUtils.isNotBlank` 不在 Spring 的 StringUtils 上）。测试范围还有 `spring-test`。

【截图待补充】IDEA 的 Project Structure 中 JDK 与 Maven 面板，以及 Tomcat 9 的 Server 配置。编写本报告的环境里没有安装 MySQL 和 Tomcat，所以启动画面要回到自己的电脑补。

## 二、实验目标

1. 按企业项目的方式完成 SSM 全注解整合，分清根容器和 Spring MVC 子容器。
2. 用 Spring 的 IoC、声明式事务、AOP，Spring MVC 的 REST、校验、拦截器和全局异常，以及 MyBatis-Plus 的通用 CRUD 与分页，完成员工管理的增删改查。
3. 接口形状与指导书一致：`/emps` 的六种操作，返回 `Result` 的 `code/message/data`。
4. 用 ApiFox 覆盖正常路径、校验、未登录、空批量删除和分页边界。
5. 对 AI 生成的配置逐项核对，把改过的地方写进本报告，不把未检查的代码直接交上去。

## 三、实验内容与过程

### 3.1 任务 1：企业开发规范

双师提纲要求整理不少于 10 条规范，作为后面写代码的自检表。我按提纲里的分层、命名、Git、评审、AI 边界、依赖冲突、事务、性能、密钥和日志写成了 `docs/enterprise-standards.md`，一共 14 条，并附了一张提交前对照表。这不是课堂录音，也没有编出勤记录。

### 3.2 任务 2：工程初始化

Maven 工程 `emp-mis`，`packaging` 为 `war`。目录按指导书放到 `com.lab.emp` 下的 config、controller、service/impl、mapper、entity、dto、common、interceptor、aspect。`.gitignore` 忽略 `target/`、`*.iml`、`.idea/`、`jdbc.local.properties` 和系统垃圾文件。war 插件设置 `failOnMissingWebXml` 为 false，工程里没有 web.xml。

### 3.3 任务 3：全注解整合

`ServletContainersInitConfig` 继承 `AbstractAnnotationConfigDispatcherServletInitializer`，根配置 `SpringConfig`，MVC 配置 `SpringMvcConfig`，映射 `/`，并注册强制 UTF-8 的 `CharacterEncodingFilter`。

`SpringConfig` 扫描 `com.lab.emp` 并排除 Controller。数据源是 Druid，连接四要素来自 `jdbc.properties`。另有一个可选的 `classpath:jdbc.local.properties`，`ignoreResourceNotFound = true`，用来覆盖占位密码。SqlSessionFactory 用的是 `MybatisSqlSessionFactoryBean`，注册了 `MybatisPlusInterceptor` 和 `PaginationInnerInterceptor(DbType.MYSQL)`，`mapUnderscoreToCamelCase` 为 true，日志实现是 Log4j。`MapperScannerConfigurer` 写成 static 方法，避免它过早创建配置类。事务管理器是 `DataSourceTransactionManager`，并打开了 `@EnableAspectJAutoProxy` 和 `@EnableTransactionManagement(proxyTargetClass = true)`。

`SpringMvcConfig` 打开 `@EnableWebMvc`，放行默认 Servlet，跨域与指导书相同（`allowedOriginPatterns("*")`，允许 GET/POST/PUT/DELETE/OPTIONS，`allowCredentials(true)`），并注册 `LoginInterceptor`，排除 `/login`、`/register` 和静态资源。

整合时我用一个不连数据库的小程序刷新了根容器，能拿到 Druid 数据源和 SqlSessionFactory。这一步发现的启动错误写在第五节。

【截图待补充】Tomcat 启动日志，确认没有 ClassNotFoundException。

### 3.4 任务 4：库表与分层

`sql/init.sql` 创建 `ssm_emp`。`emp` 沿用实验一的字段，补上非空、默认值和 `emp_name` 的普通索引。增加 `dept` 表，`emp.dept_id` 作为外键，一名员工属于一个部门。`emp.dept` 仍然保留部门名称，条件查询继续用它。四条样例是张伟、李娜、王强、赵敏。

`sys_user` 保存登录账号。测试账号 admin / 123456 的库内值是盐 `a1b2c3d4e5f60718` 加上密码后的 SHA-256 十六进制，不是明文。

实体 `Emp`、`Dept` 使用 `@TableName` 和 `@TableId(IdType.AUTO)`。`EmpMapper`、`DeptMapper` 继承 `BaseMapper`。`EmpService` 继承 `IService`，实现类继承 `ServiceImpl`。统一返回是 `Result<T>`，成功 200，失败 500，另有 401 的工厂方法。

### 3.5 任务 5：REST 接口

`EmpController` 的路径：

- `POST /emps` 新增
- `PUT /emps` 修改
- `DELETE /emps/{id}` 删除
- `DELETE /emps` 批量删除，体为整数列表
- `GET /emps/{id}` 单条
- `GET /emps` 分页，参数 name、gender、dept、page、size，默认 1 和 5

分页条件与指导书示例一致：姓名模糊，性别和部门等值，按 `empId` 降序。`page < 1` 时改成第 1 页，避免 MySQL 出现负的 OFFSET。`size < 1` 时不查数据行，只返回总数，避免 MyBatis-Plus 把负数页大小当成“不限制”。

不存在的编号返回 `code=500`、消息「员工不存在」，空的批量数组直接拒绝。新增时丢掉调用方传来的 `empId`，让数据库自增。入职日期用 `yyyy-MM-dd`，时区 GMT+8。

【截图待补充】ApiFox 中 `GET /emps?name=张&gender=男&dept=研发部&page=1&size=5` 的响应，以及控制台里带 LIMIT 的 SQL。

### 3.6 任务 6：校验、异常、登录、日志、事务

`Emp` 上：`empName` 使用 `@NotBlank` 和 `@Size(max=50)`，`salary` 使用 `@NotNull` 和 `@DecimalMin("0")`。新增和修改方法是 `@RequestBody @Valid Emp emp, BindingResult`。有错误时返回第一条字段消息。全局处理器另外接住 `MethodArgumentNotValidException`，防止以后有人漏写 `BindingResult`。

`BusinessException` 走业务失败。其他异常记完整堆栈，对外只返回「系统繁忙，请稍后重试」。

登录拦截器在会话没有 `loginUser` 时写：

```json
{"code":401,"message":"未登录"}
```

HTTP 状态同时设为 401。OPTIONS 直接放行。`POST /login` 和 `POST /register` 已从拦截路径排除。口令用 `PasswordUtil` 做 SHA-256，日志切面把 `LoginDTO` 的密码打成 `***`。

`WebLogAspect` 用 `@Around` 包住 `com.lab.emp.controller` 的方法，Log4j 输出方法、脱敏参数和耗时。切面注册在 MVC 子容器，因为父容器的切面代理不到子容器里的 Controller。`SpringMvcConfig` 因此同时扫描 controller、common 和 aspect。

事务方法 `saveWithTx`：插入后执行 `1/0`，注解为 `@Transactional(rollbackFor = Exception.class)`。另外三个方法分别演示异常被吃掉、`this.saveWithTx` 自调用、注解加在 private 方法上。不连数据库的 `TxFailureDemoTest` 用一个空数据源数提交和回滚次数。连得上库时 `EmpDatabaseOptionalTest` 才会真正插表；连不上就跳过。现象和原因写在 `docs/transaction-failure-cases.md`。

`mvn test` 在无数据库环境下：22 个用例，0 失败，1 个跳过。

【截图待补充】IDEA 或终端里的 `mvn test` 结果；连上 MySQL 后再补一条回滚前后的表数据。

### 3.7 任务 7：测试集、架构和报告

ApiFox 用例在 `apifox/`。架构和接口表在 `docs/architecture.md`。运行步骤在仓库根目录的 README。

## 四、AI 应用记录

这次实验的骨架是在 AI 编程助手里完成的。我把指导书实验二的要求交给它：包名必须是 `com.lab.emp`，依赖版本按指导书写，接口路径不能改，返回结构用 `Result`，并按 7 个任务分别提交。助手生成了工程、配置、分层代码、测试和文档草稿，我再对照指导书和 `mvn test` 的结果改。

用过的提示可以概括成下面几句：

1. 「按实验二做一个 Maven war，Spring 5.3.31 + MyBatis-Plus 3.5.3.1，不要 Spring Boot，包名 com.lab.emp，接口是 POST/PUT/DELETE/GET /emps。」
2. 「jdbc.properties 密码只能是 your_password，真实密码放到被 gitignore 的 jdbc.local.properties，PropertySource 要 ignoreResourceNotFound。」
3. 「GlobalExceptionHandler 必须进 Spring MVC 子容器。指导书的 SpringMvcConfig 只扫 controller，这样 Advice 不会生效，请改扫描范围并说明原因。」
4. 「事务要能演示回滚，也要演示异常被吃掉、自调用和 private 注解三种失效。没有 MySQL 时测试不能把构建打红。」

生成内容和我改过的地方：

| 助手先给出的做法 | 我核对后的处理 |
| --- | --- |
| 按指导书把 `SqlSessionFactoryBean` 配成分页插件 | 改成指导书允许的 `MybatisSqlSessionFactoryBean`，分页插件和驼峰都挂在它上面，并实际刷新过容器 |
| `MapperScannerConfigurer` 写成普通 `@Bean` | 改成 static，避免 BeanFactoryPostProcessor 提前初始化配置类 |
| 根容器 `@ComponentScan("com.lab.emp")` 只排除 Controller | 启动报 `No ServletContext set`。再排除 `SpringMvcConfig`。Advice 和 Aspect 也排除，改由子容器扫描 |
| 拦截器示例写成 `resp.getWriter().write("{"code":401,...}")` | 这在 Java 里不是合法字符串。改成转义后的 JSON，并补上 HTTP 401 |
| 只 `System.out` 打请求日志 | 改成 Log4j，并把密码字段换成 `***` |
| 查询不到员工时 `Result.ok(null)` | 改成 `code=500`、「员工不存在」，空批量数组也拒绝，避免 `IN ()` |
| 分页参数原样交给 `Page` | `page < 1` 调成 1，`size < 1` 只返回总数 |

AI 适合写样板和先给一个能编译的草稿。版本是不是 Tomcat 9 能用、事务注解会不会被自调用绕开、异常会不会把堆栈返回给前端，这些我是对着运行结果和指导书改的。真实数据库口令没有写进仓库，也没有放进提示词。

## 五、问题与解决方案

### 问题 1：根容器启动报 No ServletContext set

刷新 `SpringConfig` 时失败：

```text
BeanCreationException: Error creating bean with name 'resourceHandlerMapping'
Caused by: java.lang.IllegalStateException: No ServletContext set
```

原因：`@ComponentScan("com.lab.emp")` 会扫到同包里的 `SpringMvcConfig`。它是 `@Configuration`，上面还有 `@EnableWebMvc`。根容器没有 ServletContext，MVC 的资源映射一创建就失败。指导书只写了排除 `@Controller`，没有排除这个配置类。

处理：在根容器的 `excludeFilters` 里按类型排除 `SpringMvcConfig`。它只作为 `getServletConfigClasses()` 交给 DispatcherServlet。改完后根容器能创建 Druid 和 SqlSessionFactory，Mapper 扫描也能看到 `empMapper` 和 `deptMapper`。

### 问题 2：指导书里的 401 字符串不能编译

示例是：

```java
resp.getWriter().write("{"code":401,"message":"未登录"}");
```

双引号会在 `{` 后面把 Java 字符串截断，后面的 `code` 变成非法记号。这不是运行期才出现的问题，粘贴进工程就会编译失败。

处理：写成 `"{\"code\":401,\"message\":\"未登录\"}"`，响应类型设为 `application/json;charset=UTF-8`，状态码设为 401。`LoginInterceptorTest` 用 Mock 请求断言了这段 JSON。没有会话时拦截器返回 false，有 `loginUser` 时放行，OPTIONS 也放行。

### 其他在整合时排除掉的点

子容器一开始如果只扫描 controller，`@RestControllerAdvice` 只会留在根容器，而根容器的异常处理器不负责 DispatcherServlet 的请求。我把 common 包加进 `SpringMvcConfig` 的扫描，并在根容器排除 `@ControllerAdvice`。请求日志切面同样必须和 Controller 在一个容器里，所以 aspect 包也放在子容器，并在子容器打开 `@EnableAspectJAutoProxy`。

`@PropertySource` 用 `addLast` 放进环境，先声明的文件优先级更高。因此先声明 `jdbc.local.properties`，再声明 `jdbc.properties`，本机文件才能盖住占位密码。

## 六、实验总结与思考

这次把实验一的 emp 表接到了完整的 Web 层。真正费时间的不是增删改查本身，而是两个容器各扫什么、事务代理到底包住了哪一次调用、以及依赖是 `javax` 还是 `jakarta`。AI 把样板铺开以后，我用编译、无数据库的上下文刷新和 JUnit 把几处会在 Tomcat 里才爆的问题提前看掉了。ApiFox 的真实截图还要在本机补，记录里已经写了每条用例的预期，避免交一份看起来跑过、其实没有响应的报告。

规范清单里的 14 条，这次对得上的是：分层、URL、Result、提交信息、不入库口令、分页和索引、校验、401、以及无库时测试仍能通过。性能上姓名模糊查询用了 `LIKE '%xx%'`，索引帮助有限，这一点写在架构说明里，没有假装已经优化掉。

### 思考题

**1. DispatcherServlet 的请求处理流程**

请求先过 `CharacterEncodingFilter`，再进入映射为 `/` 的 DispatcherServlet。它用 HandlerMapping 按路径和 HTTP 方法找到 Controller 方法，这个过程中会执行已注册的 `HandlerInterceptor.preHandle`。未登录时拦截器返回 false，后面的适配和 Controller 都不再走。

找到方法之后，HandlerAdapter 做参数绑定：路径变量、查询参数、`@RequestBody` 的 JSON，以及 `@Valid` 触发的校验。校验结果如果紧跟着 `BindingResult`，错误留在这个对象里由方法自己返回；没有 `BindingResult` 时抛 `MethodArgumentNotValidException`，由 `@RestControllerAdvice` 接住。

方法返回 `Result`。因为类是 `@RestController`，`@ResponseBody` 生效，HttpMessageConverter（这里是 Jackson）把对象写成 JSON，不再经过视图解析器。若方法抛出的异常没被自己吃掉，DispatcherServlet 把它交给异常解析器，最终仍是一段 `Result` JSON。静态页没有对应的 Controller 时，默认 Servlet 处理 `index.html`。

**2. 根容器和 Spring MVC 子容器**

`getRootConfigClasses()` 创建的是父容器，放 Service、Mapper、数据源和事务管理器。`getServletConfigClasses()` 创建的是子容器，放 Controller、异常处理和请求日志切面。子容器能注入父容器里的 Service，父容器看不到子容器里的 Controller。

扫描必须切开。父容器若扫到 `@Controller`，Controller 会在没有 MVC 注解处理的容器里注册，请求映射不到。父容器若扫到带 `@EnableWebMvc` 的配置类，会在没有 ServletContext 时失败，这就是第五节的问题 1。`@ControllerAdvice` 和切面也要放在真正处理请求的那个容器，否则异常处理和日志都是“配了但没有包住接口”。

**3. 至少三种事务失效**

都和 AOP 代理有关。`@Transactional` 不是方法自己生效，而是调用必须经过代理对象。

1. 异常被 try/catch 吃掉。代理只能根据抛出代理的异常决定回滚。异常留在方法里，代理看到正常返回，事务提交。`saveWithSwallowedException` 就是这样，插入会留下。
2. 自调用。`saveBySelfInvocation` 里写 `this.saveWithTx(emp)`，`this` 是目标对象，不是代理，`saveWithTx` 上的注解不会开启事务。插入按自动提交落地，后面的异常没有事务可回滚。
3. 注解加在 private 方法上。Spring 默认只给 public 方法建立事务属性，private 方法不会被代理拦截。`callPrivateSave` 直接调用 `privateSave`，效果和自调用相同。

另外，默认回滚范围是运行时异常。需要回滚受检异常时要写 `rollbackFor`。本项目统一写了 `rollbackFor = Exception.class`。测试里：异常抛出代理时回滚 1 次；吃掉异常时提交 1 次；自调用和 private 方法的提交、回滚都是 0，因为根本没有事务。

**4. 拦截器和切面的时机**

拦截器是 Spring MVC 的机制，在 HandlerAdapter 调用 Controller 之前执行 `preHandle`，方法结束后还有 `postHandle` 和 `afterCompletion`。它看得到 HttpServletRequest 和 Session，适合登录这种“要不要放行”的判断。

切面是 Spring AOP，包住的是已经确定要调用的 Bean 方法。`@Around` 能在方法前后记参数和耗时，也能看见方法抛出的异常，但它不负责决定请求进不进 DispatcherServlet。登录用拦截器，请求日志用切面。两者都要放在 MVC 子容器里才作用得到 Controller：拦截器靠 `WebMvcConfigurer` 注册，切面靠子容器的自动代理。

**5. PUT 与 POST、DELETE 与 GET，以及统一返回**

POST 表示新建一份资源，重复调用可能多出一行，所以新增用 POST。PUT 表示按客户端给出的内容替换已有资源，这里按 `empId` 修改，用 PUT。GET 只查询，不改表，可以重试。DELETE 表示删除，按编号删用 `DELETE /emps/{id}`，按一组编号删用 `DELETE /emps` 加数组体。

统一的 `code/message/data` 让前端不用靠 HTTP 体的形状猜成功还是失败，校验错误、业务错误和未登录都能用同一段解析代码。HTTP 状态在未登录时我额外设成了 401，业务失败仍主要看 JSON 里的 `code`，这样和指导书的三种 code 对齐，也方便网关识别未认证。

**6. AI 辅助时哪些必须自己做**

可以交给 AI 的：目录和依赖草稿、Getter/Setter、测试数据、异常堆栈的第一种猜测、文档里的表格结构。

必须自己做的：核对 Spring 5.3、MyBatis-Plus 3.5、JDK 8 和 Tomcat 9 能不能放在同一个 war 里；确认没有引入 Spring Boot 和 `jakarta.servlet`；把事务的三种失效跑成测试而不是只保留注释；确认 401、校验和兜底异常的 JSON 与指导书一致；保证仓库里没有真实口令。AI 给出的字符串和扫描范围，我是编译和刷新容器之后才留下的。

## 交付物

- 源码与 7 次任务提交，标签 `v1.0.0`
- `sql/init.sql`
- `apifox/` 下的集合与 `测试记录.md`（截图待本机补充）
- `docs/architecture.md`
- `docs/enterprise-standards.md`
- 本报告的 Markdown 与 Word
