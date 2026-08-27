# 实验室设备借还与报修系统

面向高校实验室的设备管理系统，覆盖设备查询、借用审批、归还、故障报修、维修、逾期计费、通知和操作留痕等完整业务流程。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot、Spring Data JPA |
| 数据库 | MySQL 8、H2 测试数据库 |
| 前端 | Vue 3、Vite、Axios |
| 测试 | JUnit、Spring Boot Test |
| 工程化 | Maven、npm、PowerShell 启动脚本 |

## 核心功能

- 设备分类、条件查询、新增、状态展示和报废管理。
- 学生提交借用申请，教师、实验室管理员和学院负责人按规则分级审批。
- 设备借出、归还和逾期费用计算，业务操作同步更新设备状态。
- 故障报修、维修开始、维修进度和维修完成处理。
- 审批、归还、报修和维修事件通知，以及系统操作日志查询。
- MySQL 正式环境和 H2 本地演示环境切换。

### 技术亮点

- **职责链模式**：根据设备价值、类别和借用天数动态构建审批链，避免在业务服务中堆叠审批角色判断。
- **状态模式**：将可借用、已借出、维修中和已报废状态封装为独立行为，限制非法状态转换。
- **观察者模式**：业务服务发布审批、归还和维修事件，由通知与日志观察者分别处理副作用。
- **策略模式**：针对普通设备、计算机设备和精密设备使用不同逾期计费策略，便于扩展新类别。
- 通过全局异常处理和 DTO 隔离接口模型，避免控制层直接暴露持久化实体。

## 个人工作

本项目由本人独立完成，主要工作包括：

- 梳理设备借用、分级审批、归还、报修和维修的业务规则及状态流转。
- 设计数据库实体、Repository、Service、REST 接口和前端操作流程。
- 将职责链、状态、观察者和策略模式落地到真实业务，而不是只编写独立模式示例。
- 实现审批记录、消息通知和操作日志，使关键业务过程可查询、可追踪。
- 配置 MySQL/H2 双环境、初始化数据、自动化测试和公开仓库工程化整理。

## 本地运行

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 20+
- MySQL 8；仅快速演示时可使用内置 H2 配置

### MySQL 方式

1. 配置数据库环境变量：

```powershell
$env:LAB_DB_URL = "jdbc:mysql://localhost:3306/lab_equipment?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:LAB_DB_USERNAME = "root"
$env:LAB_DB_PASSWORD = "你的数据库密码"
```

2. 初始化数据库：

```bash
mysql -u root -p < src/main/resources/db/init-mysql.sql
```

3. 启动后端：

```bash
mvn spring-boot:run
```

也可以在 Windows 下运行：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run-backend-mysql.ps1
```

### H2 快速演示

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run-backend-dev.ps1
```

### 启动前端

```bash
cd frontend
npm ci
npm run dev
```

前端默认访问 `http://localhost:5173`，后端默认监听 `http://localhost:8080`。

当前课程设计版本通过页面切换演示用户，并使用请求头 `X-User-Id` 传递用户编号。该方式仅用于本地流程演示，不是生产级认证方案。

## 项目结构

```text
lab-equipment-system/
├── src/main/java/com/example/labdesign/
│   ├── controller/             REST 接口
│   ├── service/                业务服务
│   ├── repository/             数据访问
│   ├── entity/                 领域实体
│   └── pattern/                审批、状态、事件和计费模式
├── src/main/resources/         环境配置与数据库脚本
├── src/test/                   后端测试
├── frontend/                   Vue 3 前端
├── scripts/                    Windows 启动脚本
└── docs/                       接口与设计图
```

## 测试与安全

- 后端执行 `mvn test`：`4` 项业务模式测试通过。
- 前端执行 `npm ci` 和 `npm run build`：生产构建通过。
- 数据库密码、`.env`、日志、课程报告、IDE 配置、`target`、`node_modules` 和 `dist` 均不进入版本控制。
- 后续可接入 Spring Security 与 JWT，替换当前演示用户切换机制。
