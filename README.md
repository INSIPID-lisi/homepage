# Homepage

个人主页项目，展示个人信息、社交链接、博客帖子以及追番记录。前后端分离架构，带 JWT 认证和邮箱验证。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite |
| 后端 | Spring Boot 3.5 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 8 |
| UI 库 | Element Plus 2 + @element-plus/icons-vue |
| 构建 | Maven (后端) / Vite (前端) |
| 认证 | JWT (jjwt 0.12.6) + BCrypt |
| 邮件 | Jakarta Mail (spring-boot-starter-mail) |

## 功能模块

- **导航系统** — 扇形转盘导航（拖拽旋转 / 点击对齐）+ 弹出侧栏 + 长按固定 + 键盘快捷键 Alt+1~4
- **个人信息展示** — 头像、名称、个人简介、社交链接
- **帖子管理** — 分类（正规推文 / 碎碎念）、模糊搜索、置顶
- **追番记录** — 番剧封面网格展示、评价管理、番剧卡片上传
- **用户认证** — 邮箱注册/登录、密码登录、验证码登录、JWT 鉴权
- **角色管理** — 第一位注册的账号自动成为管理员，其余为普通用户
- **后台管理** — 管理员增删改操作（JWT role 校验）
- **设置页面** — 修改密码（旧密码验证 / 邮箱验证码两种模式）、切换账号、退出登录

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

### 2. 配置数据库和邮件

编辑 `backend/src/main/resources/application-dev.yml`，填入数据库连接信息和邮件授权码：

```yaml
hp:
  db:
    url: jdbc:mysql://localhost:3306/homepage_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  mail:
    password: your_email_authorization_code
```

邮件使用 163 SMTP（smtp.163.com:465），需在 163 邮箱设置中开启 POP3/SMTP 服务并生成授权码。

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
│       │   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
│       │   ├── MailConfig.java              # JavaMail 配置 (SSL/TLSv1.2)
│       │   ├── SecurityConfig.java          # Spring Security 配置
│       │   └── WebConfig.java              # CORS + 静态资源映射
│       ├── controller/               # REST 控制器
│       │   ├── AdminController.java
│       │   ├── AnimeController.java
│       │   ├── AuthController.java   # 登录/注册/验证码
│       │   ├── PostController.java
│       │   ├── ProfileController.java
│       │   └── UploadController.java
│       ├── dto/                      # 数据传输对象
│       │   ├── AuthResponse.java
│       │   ├── CodeLoginRequest.java
│       │   ├── LoginRequest.java
│       │   ├── PasswordChangeRequest.java
│       │   ├── RegisterRequest.java
│       │   └── Result.java           # 统一响应包装
│       ├── entity/                   # 数据实体
│       │   ├── Anime.java
│       │   ├── AnimeReview.java
│       │   ├── Post.java
│       │   ├── SocialLink.java
│       │   ├── User.java
│       │   └── UserProfile.java
│       ├── exception/                # 异常体系
│       │   ├── BusinessException.java
│       │   └── ErrorCode.java
│       ├── mapper/                   # MyBatis-Plus Mapper
│       ├── service/                  # 业务接口
│       │   ├── AnimeService.java
│       │   ├── AuthService.java
│       │   ├── EmailService.java
│       │   ├── PostService.java
│       │   ├── SocialLinkService.java
│       │   └── UserProfileService.java
│       ├── service/impl/             # 业务实现
│       ├── util/                     # 工具类
│       │   ├── JwtUtil.java          # JWT 生成/解析
│       │   └── SecurityUtil.java     # 当前用户/管理员校验
│       └── HomepageApplication.java
│
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── api/index.ts              # Axios 请求封装 + API 函数
│       ├── components/               # 公共组件
│       │   ├── NavDial.vue           # 扇形转盘导航
│       │   ├── ProfileCard.vue
│       │   ├── Sidebar.vue
│       │   ├── SidebarPopup.vue      # 弹出侧栏
│       │   └── SocialLinks.vue
│       ├── composables/
│       │   └── useKeyboardShortcuts.ts  # 键盘快捷键 (Alt+1~4)
│       ├── layouts/DefaultLayout.vue # 整体布局（含转盘/侧栏联动）
│       ├── navigation.ts             # 导航项共享常量
│       ├── router/index.ts           # 路由配置（含 /login、/settings）
│       ├── store/index.ts            # Pinia 状态管理
│       ├── styles/main.css           # 全局样式 + Element Plus 暗色主题
│       └── views/                    # 页面
│           ├── HomeView.vue
│           ├── LoginView.vue         # 登录/注册页
│           ├── PostsView.vue
│           ├── PostDetailView.vue
│           ├── AnimeListView.vue
│           ├── AnimeDetailView.vue
│           └── SettingsView.vue      # 切换账号 / 退出登录
```

## API 概览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/profile | 获取个人信息 |
| PUT | /api/profile | 修改个人信息（管理员） |
| GET | /api/social-links | 获取社交链接 |
| PUT | /api/social-links | 修改社交链接（管理员） |
| GET | /api/posts | 帖子列表（支持 `?type=` 和 `?search=`） |
| GET | /api/posts/{id} | 帖子详情 |
| POST | /api/posts | 创建帖子（管理员） |
| PUT | /api/posts/{id} | 修改帖子（管理员） |
| DELETE | /api/posts/{id} | 删除帖子（管理员） |
| GET | /api/anime | 番剧列表（含最新评价） |
| GET | /api/anime/{id} | 番剧详情（含所有评价） |
| POST | /api/anime | 添加番剧（管理员） |
| PUT | /api/anime/{id} | 修改番剧（管理员） |
| DELETE | /api/anime/{id} | 删除番剧（管理员） |
| POST | /api/anime/{id}/reviews | 添加评价（管理员） |
| PUT | /api/anime/{id}/reviews/{reviewId} | 修改评价（管理员） |
| DELETE | /api/anime/{id}/reviews/{reviewId} | 删除评价（管理员） |
| POST | /api/upload | 上传图片（管理员） |
| POST | /api/auth/register | 注册（邮箱+密码+验证码） |
| POST | /api/auth/login | 密码登录 |
| POST | /api/auth/login-code | 验证码登录 |
| POST | /api/auth/code | 发送邮箱验证码 |
| PUT | /api/auth/password | 修改密码（旧密码或邮箱验证码） |
| GET | /api/weather | 获取今日天气与 7 天预报（OpenMeteo） |
| GET | /api/auth/me | 获取当前用户信息 |
| GET | /api/admin/check | 检查是否为管理员 |

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
- `spring.servlet.multipart.max-file-size` — 单文件上传上限（默认 `10MB`）
- `spring.mail` — 邮件服务器配置（163 SMTP，详见 application-dev.yml）

JWT 配置（`application-dev.yml`）：

- `app.jwt.secret` — JWT 签名密钥（256位+）
- `app.jwt.expiration` — Token 过期时间（默认 86400000ms = 24h）

## 认证说明

- 所有 `/api/auth/**` 和 `/uploads/**` 无需认证
- 其他 `/api/**` 接口允许匿名访问，但增删改操作需管理员角色
- 第一位注册的账号自动成为管理员，后续注册的均为普通用户
- 登录后 JWT 存入 `localStorage`，请求时通过 `Authorization: Bearer <token>` 携带
