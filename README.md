# AgentHub

多 Agent 协作平台 - 类飞书/微信交互形态的 AI 对话系统

## 项目简介

AgentHub 是一个支持单聊、群聊协作的多 Agent 平台，用户可以与不同 AI Agent 交流，Agent 之间可以协同完成任务。

### 核心功能

- **单聊**：用户与单个 Agent 对话
- **群聊协作**：@ 多个 Agent，由 Orchestrator 负责任务分派和结果汇总
- **富媒体 Artifact**：代码块、Diff 卡片、网页预览、文件附件
- **用户自建 Agent**：通过配置 System Prompt 创建自定义 Agent
- **多平台接入**：支持 MiniMax、Coze 等主流 Agent 平台

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 18 + TypeScript + Vite + Ant Design |
| 后端 | Spring Boot 3.x + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 实时通信 | WebSocket + STOMP |
| 鉴权 | JWT |
| Agent 调用 | OkHttp + Retrofit |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- Docker (for MySQL)
- Maven 3.8+

### 1. 启动 MySQL

```bash
docker-compose up -d
```

初始化数据库：

```bash
mysql -h localhost -u root -proot123 < backend/src/main/resources/schema.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：http://localhost:8080

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：http://localhost:5173

### 4. 访问应用

打开浏览器访问 http://localhost:5173

1. 注册账号
2. 登录
3. 创建会话开始聊天

## 项目结构

```
AgentHub/
├── backend/                 # Java 后端
│   ├── src/main/java/com/agenthub/
│   │   ├── controller/      # REST API
│   │   ├── service/        # 业务逻辑
│   │   ├── repository/     # 数据访问
│   │   ├── model/entity/   # 数据库实体
│   │   ├── agent/          # Agent 核心
│   │   │   ├── adapter/    # 外部 Agent 适配器
│   │   │   ├── core/       # Agent 核心逻辑
│   │   │   └── orchestrator/# 群聊主控
│   │   ├── security/       # JWT 鉴权
│   │   └── config/         # 配置类
│   └── src/main/resources/
│       ├── application.yml
│       └── schema.sql      # 数据库建表脚本
│
├── frontend/                # React 前端
│   └── src/
│       ├── api/            # API 请求封装
│       ├── components/      # 公共组件
│       ├── pages/           # 页面
│       ├── stores/          # Zustand 状态
│       └── types/           # TypeScript 类型
│
└── docker-compose.yml      # MySQL 容器
```

## API 文档

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 注册 |
| POST | /api/auth/login | 登录 |

### 会话

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/conversations | 获取会话列表 |
| POST | /api/conversations | 创建会话 |
| GET | /api/conversations/{id}/messages | 获取消息列表 |
| POST | /api/conversations/{id}/messages | 发送消息 |

## 开发指南

### 后端开发

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包
mvn clean package -DskipTests
```

### 前端开发

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建
npm run build
```

## 演示链路

1. ✅ 用户注册/登录
2. ✅ 创建会话
3. ✅ 发送消息
4. ✅ Agent 回复
5. 🔄 群聊 @ 多个 Agent（开发中）
6. 🔄 Artifact 卡片展示（开发中）

## 后续计划

- [ ] Orchestrator 群聊协作逻辑
- [ ] 多 Agent 适配器接入
- [ ] 富媒体 Artifact 卡片
- [ ] 用户自建 Agent
- [ ] 部署能力

## License

MIT
