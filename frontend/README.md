# Vue 前端

这是实验室设备借还报修系统的 Vue 3 + Vite 前端，接口通过 Vite 代理转发到 Spring Boot 后端。

## 启动

```bash
cd frontend
npm install
npm run dev
```

访问：`http://localhost:5173`

启动前请先运行后端服务：`http://localhost:8080`。

## 构建

```bash
npm run build
```

构建结果位于 `frontend/dist/`。


如需让 Spring Boot 直接托管 Vue 构建后的页面，可运行：

```bash
cd frontend
npm run build:backend
```

该命令会把构建结果输出到 `src/main/resources/static/`。
