# Homepage

个人主页项目，展示个人信息、社交链接、博客帖子以及追番记录。前后端分离架构。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite |
| 后端 | Spring Boot 3.5 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 8 |
| UI 库 | Element Plus 2 + @element-plus/icons-vue |
| 构建 | Maven (后端) / Vite (前端) |

## 功能模块

- **个人信息展示** — 头像、名称、个人简介、社交链接
- **帖子管理** — 分类（正规推文 / 碎碎念）、模糊搜索、置顶
- **追番记录** — 番剧封面网格展示、评价管理、番剧卡片上传
- **后台管理** — IP 白名单鉴权（管理员增删改操作）

## 快速开始

### 前置要求

- JDK 21+
- Maven 3.8+
- Node.js 20+
- MySQL 8.0+

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS homepage_db DEFAULT CHARSET utf8mb4;
USE homepage_db;
SOURCE backend/src/main/resources/db/init.sql;
```

### 2. 配置数据库

编辑 `backend/src/main/resources/application-dev.yml`，填入数据库连接信息：

```yaml
hp:
  db:
    url: jdbc:mysql://localhost:3306/homepage_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，已配置代理转发 `/api` 和 `/uploads` 到后端。

### 5. 初始化数据

执行后可在 `user_profile` 和 `social_link` 表中插入初始数据：

```sql
INSERT INTO user_profile (name, avatar, bio) VALUES ('Your Name', 'avatar.png', 'Hello World');
INSERT INTO social_link (platform, url, icon) VALUES ('GitHub', 'https://github.com/xxx', 'github');
```

图片文件存放于 `~/homepage/images/`，通过 `/uploads/` 路径访问。

## 项目结构

```
homepage/
├── backend/                          # Spring Boot 后端
│   └── src/main/java/com/homepage/
│       ├── config/                   # 全局配置
│       │   ├── GlobalExceptionHandler.java  # 全局异常处理
│       │   ├── SecurityConfig.java          # Spring Security 配置
│       │   └── WebConfig.java              # CORS + 静态资源映射
│       ├── controller/               # REST 控制器
│       │   ├── AdminController.java
│       │   ├── AnimeController.java
│       │   ├── PostController.java
│       │   ├── ProfileController.java
│       │   └── UploadController.java
│       ├── dto/                      # 数据传输对象
│       │   ├── AnimeDetailVO.java
│       │   ├── AnimeVO.java
│       │   └── Result.java           # 统一响应包装
│       ├── entity/                   # 数据实体
│       │   ├── Anime.java
│       │   ├── AnimeReview.java
│       │   ├── Post.java
│       │   ├── SocialLink.java
│       │   └── UserProfile.java
│       ├── exception/                # 异常体系
│       │   ├── BusinessException.java
│       │   └── ErrorCode.java
│       ├── mapper/                   # MyBatis-Plus Mapper
│       ├── service/                  # 业务接口
│       └── service/impl/             # 业务实现
│
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── api/index.ts              # Axios 请求封装 + API 函数
│       ├── components/               # 公共组件
│       │   ├── ProfileCard.vue
│       │   ├── Sidebar.vue
│       │   └── SocialLinks.vue
│       ├── layouts/DefaultLayout.vue # 整体布局
│       ├── router/index.ts           # 路由配置
│       ├── store/index.ts            # Pinia 状态管理
│       ├── styles/main.css           # 全局样式 + Element Plus 暗色主题覆盖
│       └── views/                    # 页面
│           ├── HomeView.vue          # 首页
│           ├── PostsView.vue         # 帖子列表
│           ├── PostDetailView.vue    # 帖子详情
│           ├── AnimeListView.vue     # 追番列表
│           └── AnimeDetailView.vue   # 番剧详情
```

## API 概览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/profile | 获取个人信息 |
| GET | /api/social-links | 获取社交链接 |
| GET | /api/posts | 帖子列表（支持 `?type=` 和 `?search=`） |
| GET | /api/posts/{id} | 帖子详情 |
| POST | /api/posts | 创建帖子 |
| PUT | /api/posts/{id} | 修改帖子 |
| DELETE | /api/posts/{id} | 删除帖子 |
| GET | /api/anime | 番剧列表（含最新评价） |
| GET | /api/anime/{id} | 番剧详情（含所有评价） |
| POST | /api/anime | 添加番剧 |
| PUT | /api/anime/{id} | 修改番剧 |
| DELETE | /api/anime/{id} | 删除番剧 |
| POST | /api/anime/{id}/reviews | 添加评价 |
| PUT | /api/anime/{id}/reviews/{reviewId} | 修改评价 |
| DELETE | /api/anime/{id}/reviews/{reviewId} | 删除评价 |
| POST | /api/upload | 上传图片（MultipartFile） |
| GET | /api/admin/check | 检查是否为管理员 IP |

所有响应格式统一为：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## 配置说明

关键配置项位于 `backend/src/main/resources/application.yml`：

- `app.upload.path` — 图片上传路径（默认 `homepage/images`，相对于用户目录）
- `app.upload.url-prefix` — 图片访问路径前缀（默认 `/uploads`）
- `app.security.admin-ips` — 管理员 IP 白名单，逗号分隔
- `spring.servlet.multipart.max-file-size` — 单文件上传上限（默认 `10MB`）
