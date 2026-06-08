# AgentHub

多 Agent 协作平台 - 类飞书/微信交互形态的 AI 对话系统

## 一键部署

```bash
# 克隆项目
git clone https://github.com/comioko/agenthub.git
cd agenthub

# 一键启动（需要 Docker 和 Docker Compose）
docker-compose up -d

# 访问应用
# 前端: http://localhost
# 后端 API: http://localhost:8080
```

首次启动会自动：
1. 初始化 MySQL 数据库
2. 构建并启动后端服务
3. 构建并启动前端服务

## 快速开始（开发模式）

### 前置要求
- JDK 21+
- Node.js 18+
- Docker (for MySQL)
- Maven 3.8+

### 1. 启动 MySQL

```bash
docker-compose up -d mysql
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

## 核心功能

- **单聊**：用户与单个 Agent 对话
- **群聊协作**：@ 多个 Agent，由 Orchestrator 负责任务分派和结果汇总
- **富媒体 Artifact**：代码块、Diff 卡片、网页预览
- **用户自建 Agent**：通过配置 System Prompt 创建自定义 Agent
- **多平台接入**：支持 MiniMax、Coze 等主流 Agent 平台

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 18 + TypeScript + Vite + Ant Design |
| 后端 | Spring Boot 3.x + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 实时通信 | WebSocket |
| 鉴权 | JWT |
| 部署 | Docker + Docker Compose + Nginx |

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
│   └── Dockerfile
│
├── frontend/                # React 前端
│   ├── src/
│   │   ├── api/            # API 请求封装
│   │   ├── components/      # 公共组件
│   │   ├── pages/           # 页面
│   │   └── websocket/       # WebSocket
│   ├── Dockerfile
│   └── nginx.conf           # Nginx 配置
│
├── docs/                    # 开发规范文档
│   ├── specs/              # 功能规范
│   ├── skills/             # 开发技能
│   └── rules/              # 开发规则
│
├── docker-compose.yml       # Docker 编排
└── README.md
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

### Agent

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/agents | 获取 Agent 列表 |
| POST | /api/agents | 创建 Agent |

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

## 环境变量

### Docker 部署

在 `.env.docker` 或环境变量中配置：

```env
MYSQL_ROOT_PASSWORD=root123
JWT_SECRET=your-jwt-secret
MINIMAX_API_KEY=your-minimax-api-key
COZE_API_KEY=your-coze-api-key
```

### 开发模式

复制 `backend/src/main/resources/application-local.yml.example` 为 `application-local.yml` 并配置。

## 演示链路

1. ✅ 用户注册/登录
2. ✅ 创建会话
3. ✅ 发送消息
4. ✅ Agent 回复
5. 🔄 群聊 @ 多个 Agent（开发中）
6. 🔄 Artifact 卡片展示（开发中）

## License

MIT
