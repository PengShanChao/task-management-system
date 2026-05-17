# Task Management System

全栈任务管理系统，支持任务协作、仪表盘统计和技术新闻聚合。48 小时 MVP，AI 辅助开发。

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.4.5 |
| ORM | MyBatis-Plus | 3.5.5 |
| 安全 | Spring Security + JWT (jjwt) | 0.12.x |
| 数据库 | MySQL (prod) / H2 (dev) | 8.0.37 / 2.3.232 |
| 前端框架 | Vue 3 (Composition API) | 3.5.x |
| 构建工具 | Vite | 6.x |
| UI 库 | Element Plus | 2.x |
| 图表 | ECharts | 5.x |

---

## 功能

### 用户认证
- 用户注册 / 登录
- JWT 无状态认证
- RBAC 角色权限（USER / ADMIN）

### 任务管理
- 任务 CRUD（创建、编辑、删除、详情）
- 状态流转：TODO → IN_PROGRESS → DONE
- 优先级：LOW / MEDIUM / HIGH
- 截止日期倒计时提醒
- 多维度筛选（状态、优先级、指派人、截止日期）
- 表格 / 卡片两种视图
- 导出 CSV / Excel

### 仪表盘
- 任务统计（总数、各状态数量、完成率）
- 高优先级/逾期任务统计
- 完成率饼图

### 技术新闻
- 定时 RSS 抓取（oschina、infoq、36kr）
- 新闻列表分页浏览
- 按来源 / 关键字筛选
- 手动触发刷新
- 任务关联新闻（链接/取消链接/自动推荐）

---

## 项目结构

```
task-management-system/
├── backend/
│   └── src/main/java/com/taskmanager/
│       ├── controller/    # REST 控制器（Auth/Task/News/Dashboard/Export/User）
│       ├── service/       # 业务逻辑层
│       ├── mapper/        # MyBatis-Plus 数据访问
│       ├── entity/        # 数据库实体
│       ├── dto/           # 请求/响应 DTO
│       ├── config/        # Spring Security、JWT、CORS、MyBatis-Plus 配置
│       └── common/        # 统一响应、全局异常处理
├── frontend/
│   └── src/
│       ├── views/         # 页面组件（Login/TaskList/Dashboard/News/Layout）
│       ├── components/    # 可复用组件（TaskDialog）
│       ├── api/           # Axios API 封装
│       ├── router/        # Vue Router + 路由守卫
│       ├── stores/        # Pinia 状态管理
│       └── utils/         # Axios 拦截器
└── docs/                  # 设计文档、MySQL 建表脚本
```

---

## 数据库表

| 表名 | 说明 |
|------|------|
| `tb_user` | 用户（用户名、密码 bcrypt、角色） |
| `task` | 任务（标题、状态、优先级、截止日期、逻辑删除） |
| `news` | 技术新闻（标题、摘要、URL、来源） |
| `task_news_relation` | 任务-新闻多对多关联 |

建表脚本：[docs/mysql-schema.sql](docs/mysql-schema.sql)

---

## API 接口

所有接口统一响应：`{ code: number, message: string, data: T }`

### 认证 — `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/login` | 登录 | 否 |
| POST | `/api/auth/register` | 注册 | 否 |

### 任务 — `/api/tasks`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/tasks` | 任务列表（分页+筛选） | 是 |
| POST | `/api/tasks` | 创建任务 | 是 |
| GET | `/api/tasks/{id}` | 任务详情 | 是 |
| PUT | `/api/tasks/{id}` | 更新任务 | 是 |
| DELETE | `/api/tasks/{id}` | 删除任务（逻辑删除） | 是 |
| GET | `/api/tasks/{id}/news` | 任务关联新闻 | 是 |
| POST | `/api/tasks/{id}/news/{newsId}` | 关联新闻 | 是 |
| DELETE | `/api/tasks/{id}/news/{newsId}` | 取消关联 | 是 |

### 新闻 — `/api/news`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/news` | 新闻列表（分页+筛选） | 是 |
| GET | `/api/news/search` | 关键字搜索 | 是 |
| GET | `/api/news/sources` | 获取所有来源 | 是 |
| GET | `/api/news/related` | 当前用户相关新闻 | 是 |
| POST | `/api/news/refresh` | 手动触发抓取 | 是 |

### 仪表盘 — `/api/dashboard`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/dashboard/stats` | 当前用户任务统计 | 是 |

### 导出 — `/api/export`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/export/tasks?format=csv` | 导出 CSV | 是 |
| GET | `/api/export/tasks?format=xlsx` | 导出 Excel | 是 |

---

## 本地运行

### 前置条件
- Java 17+ (Eclipse Temurin 17.0.19)
- Maven 3.9+
- Node.js 18+
- MySQL 8.0.37

### 1. 克隆项目

```bash
git clone https://github.com/PengShanChao/task-management-system.git
cd task-management-system
```

### 2. 数据库初始化

```bash
mysql -u root -p < docs/mysql-schema.sql
```

### 3. 后端配置

```bash
cd backend
# 复制模板并填入你的数据库密码
cp src/main/resources/application-template.yml src/main/resources/application.yml
# 修改 application.yml 中的 username / password / jwt.secret
```

### 4. 启动后端

```bash
mvn spring-boot:run
# 启动于 http://localhost:8081
```

或者用 H2 开发模式（无需 MySQL）：

```bash
# 修改 application.yml: spring.profiles.active: dev
mvn spring-boot:run
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
# 启动于 http://localhost:5173
```

## 遇到的问题与解决

| 问题 | 解决 |
|------|------|
| 表名 `user` 与 MySQL 关键字冲突 | 改为 `tb_user` |
| H2 与 MySQL SQL 语法不兼容 | Spring Profile 分离 dev/prod 配置 |
| MySQL `Public Key Retrieval is not allowed` | JDBC URL 追加 `allowPublicKeyRetrieval=true` |
| Source 下拉框始终为空 | 前端修复变量赋值 + 后端新增 `/api/news/sources` |
| TaskDialog 模式判断脆弱 | 调用方显式区分 create/edit/view 场景 |
| AI 生成中文异常消息违反规范 | 统一改为英文 |

详见 [docs/design-doc.md](docs/design-doc.md) 第 5-6 章。

---

## AI 工具使用

| 工具 | 用途 |
|------|------|
| Claude Code | 主力编码、调试、Bug 修复、架构设计 |
| ChatGPT | 技术方案讨论、RSS 方案选型 |
| DeepSeek | 中文文档生成、SQL 校验 |
| GitHub Copilot | IDE 代码补全、模板生成 |

---

## 作者

Peng Shanchao — 实习全栈实践测试项目
