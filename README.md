# 实验室设备借还与报修系统

基于 Spring Boot、Vue 3 和 MySQL 实现的实验室设备管理系统，覆盖设备查询、借用申请、多级审批、归还、报修、维修、逾期费用、通知和操作日志等流程。

项目是《软件设计》课程设计，重点在真实业务流程中应用职责链、状态、观察者和策略等设计模式。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、原生 CSS |
| 后端 | Java 17、Spring Boot、Spring Data JPA |
| 数据库 | MySQL 8、H2 测试数据库 |
| 构建工具 | Maven、npm |
| 测试 | JUnit、Spring Boot Test |

## 核心功能

- 实验室设备分类、查询和状态展示
- 学生提交设备借用申请
- 根据设备价值、类别和借用天数动态计算审批流程
- 教师、实验室管理员和学院负责人分级审批
- 设备借出、归还、维修和报废状态管理
- 设备故障报修、维修进度跟踪和维修完成处理
- 按设备类别计算逾期费用
- 审批、归还和维修事件通知
- 系统操作日志和业务记录查询
- MySQL 正式环境与 H2 演示环境切换

## 设计模式

### 职责链模式

`pattern/approval` 根据设备价值、设备类别和借用天数构建审批链，避免在业务代码中堆叠大量条件分支。

### 状态模式

`pattern/state` 将设备的可借用、已借出、维修中和已报废等状态封装为独立状态行为，限制不同状态下允许执行的操作。

### 观察者模式

`pattern/event` 在申请审批、设备归还、故障报修和维修完成后发布业务事件，由通知与日志模块统一处理。

### 策略模式

`pattern/fee` 针对不同设备类别采用不同逾期费用计算策略，便于后续增加新的设备类型和计费规则。

## 项目结构

```text
lab-equipment-system/
├── src/main/java/                  Spring Boot 业务代码
│   └── com/example/labdesign/
│       ├── controller/             REST 接口
│       ├── service/                业务服务
│       ├── repository/             数据访问
│       ├── entity/                 数据实体
│       └── pattern/                设计模式实现
├── src/main/resources/             配置与数据库脚本
├── src/test/                       后端测试
├── frontend/                       Vue 3 前端
├── scripts/                        Windows 启动脚本
├── docs/                           设计文档与示意图
├── .env.example                    环境变量示例
└── README.md                       项目说明
```

## 快速开始

### 1. 环境要求

- JDK 17 或更高版本
- Maven 3.8 或更高版本
- MySQL 8
- Node.js 20 或更高版本

### 2. 配置数据库

系统通过环境变量读取数据库配置：

- `LAB_DB_URL`：MySQL JDBC 地址
- `LAB_DB_USERNAME`：MySQL 用户名
- `LAB_DB_PASSWORD`：MySQL 密码

Windows PowerShell 示例：

```powershell
$env:LAB_DB_USERNAME = "root"
$env:LAB_DB_PASSWORD = "你的数据库密码"
```

初始化数据库：

```bash
mysql -u root -p < src/main/resources/db/init-mysql.sql
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

如果只想快速演示，可使用 H2 内存数据库脚本：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run-backend-dev.ps1
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`

Vite 已配置 `/api` 代理到后端服务。

## 演示用户

- `student01`：学生
- `labadmin`：实验室管理员
- `teacher`：指导教师
- `dean`：学院负责人

当前课程设计版本通过页面切换演示用户，并使用请求头 `X-User-Id` 传递用户编号。该方式仅用于本地流程演示，不属于生产级身份认证方案。

## 测试与验证

本展示版本已完成以下验证：

- 后端执行 `mvn test`：`4` 项测试通过
- 前端执行 `npm ci` 和 `npm run build`：生产构建成功

## 敏感信息处理

- 数据库账号和密码通过环境变量读取
- `.env`、IDE 配置、日志和构建产物均被忽略
- 课程设计报告和个人信息文件不进入 GitHub 仓库
- `target`、`node_modules` 和前端构建目录不进入版本控制

## 后续改进方向

- 接入 Spring Security 与 JWT，替换演示用户切换机制
- 增加设备图片与附件对象存储
- 增加分页、条件检索和导出功能
- 增加 Docker Compose 一键部署
- 完善前端组件拆分和端到端测试
